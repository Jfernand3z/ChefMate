import React, { useState, useRef } from 'react';
import { useNavigate } from 'react-router-dom';
import '../auth.css';
import { RECAPTCHA_SITE_KEY } from '../utils/apiConfig';
import ReCAPTCHA from 'react-google-recaptcha';
import { api } from '../services/api';
import { TokenManager } from '../services/tokenManager';
import { Eye, EyeOff } from 'lucide-react';
import Logo from '../assets/LogoChef.png';

export default function Auth() {
  const [isRegisterActive, setIsRegisterActive] = useState(false);
  const navigate = useNavigate();

  // Loading/Error states
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [showPassword, setShowPassword] = useState(false);

  // Form states
  const [registerForm, setRegisterForm] = useState({ username: '', email: '', password: '' });
  const [loginForm, setLoginForm] = useState({ username: '', password: '' });
  const [captchaToken, setCaptchaToken] = useState<string | null>(null);
  const recaptchaRef = useRef<ReCAPTCHA>(null);

  const togglePanel = () => {
    setIsRegisterActive(!isRegisterActive);
    setError(null); // Clear errors when toggling
    setRegisterForm({ username: '', email: '', password: '' });
    setLoginForm({ username: '', password: '' });
    setShowPassword(false);
  };

  const validateEmail = (email: string) => /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email);

  const validatePassword = (password: string) => {
    if (!password) return false;
    return password.length >= 6 && /[A-Z]/.test(password) && /[a-z]/.test(password) && /\d/.test(password) && !/\s/.test(password);
  };

  const getPasswordFeedback = (password: string) => {
    if (!password) return null;
    if (password.length < 6) return 'Debe tener al menos 6 caracteres.';
    if (!/[A-Z]/.test(password)) return 'Debe contener una letra mayúscula.';
    if (!/[a-z]/.test(password)) return 'Debe contener una letra minúscula.';
    if (!/\d/.test(password)) return 'Debe contener un número.';
    if (/\s/.test(password)) return 'No debe contener espacios.';
    return null;
  };

  const calculateStrength = (password: string) => {
    if (!password || /\s/.test(password)) return 0;
    let score = 0;
    if (password.length >= 6) score += 1;
    if (password.length >= 8) score += 1;
    if (/[A-Z]/.test(password)) score += 1;
    if (/[a-z]/.test(password)) score += 1;
    if (/\d/.test(password)) score += 1;
    return score;
  };

  const renderStrengthMeter = (password: string) => {
    if (!password) return null;
    const score = calculateStrength(password);

    let colorClass = 'bg-red-500';
    let textClass = 'text-red-500';
    let textLabel = 'Muy Débil';

    if (score === 2) { colorClass = 'bg-orange-500'; textClass = 'text-orange-500'; textLabel = 'Débil'; }
    else if (score === 3) { colorClass = 'bg-yellow-500'; textClass = 'text-yellow-500'; textLabel = 'Regular'; }
    else if (score === 4) { colorClass = 'bg-green-400'; textClass = 'text-green-400'; textLabel = 'Fuerte'; }
    else if (score === 5) { colorClass = 'bg-green-600'; textClass = 'text-green-600'; textLabel = 'Muy Fuerte'; }

    if (score > 0 && !validatePassword(password)) {
      colorClass = 'bg-red-500';
      textClass = 'text-red-500';
      textLabel = 'Incompleta';
    }

    return (
      <div className="w-full mt-2 px-1">
        <div className="flex justify-between items-center mb-1">
          <span className="text-xs text-text-secondary opacity-80">Nivel de seguridad:</span>
          <span className={`text-xs font-semibold ${textClass}`}>{textLabel}</span>
        </div>
        <div className="flex gap-1 h-1 w-full bg-gray-200/30 rounded overflow-hidden">
          {[1, 2, 3, 4, 5].map((level) => (
            <div
              key={level}
              className={`h-full flex-1 transition-all duration-300 ${score >= level ? colorClass : 'bg-transparent'}`}
            />
          ))}
        </div>
      </div>
    );
  };

  const isRegisterValid =
    registerForm.username.trim() !== '' &&
    validateEmail(registerForm.email) &&
    validatePassword(registerForm.password);

  const isLoginValid =
    validateEmail(loginForm.username) &&
    loginForm.password.trim() !== '' &&
    captchaToken !== null;

  const handleRegister = async (e: React.FormEvent) => {
    e.preventDefault();
    setLoading(true);
    setError(null);
    try {
      const resp = await api.post('/users/register', registerForm);
      if (resp.status !== 200 && resp.status !== 201) {
        throw new Error('Error al registrar cuenta');
      }
      // Registration successful, switch to login panel
      setIsRegisterActive(false);
      alert('¡Registro exitoso! Por favor inicia sesión.');
    } catch (err: any) {
      const serverFeedback = err.response?.data?.detail || err.response?.data?.errors?.[0]?.message;
      setError(serverFeedback || err.message);
    } finally {
      setLoading(false);
    }
  };

  const handleLogin = async (e: React.FormEvent) => {
    e.preventDefault();
    setLoading(true);
    setError(null);
    try {
      if (!captchaToken) {
        throw new Error('Por favor, completa el reCAPTCHA antes de iniciar sesión.');
      }

      const formData = new URLSearchParams();
      // The backend uses OAuth2PasswordRequestForm which expects "username" field (we pass the email here)
      formData.append('username', loginForm.username);
      formData.append('password', loginForm.password);
      formData.append('captcha_token', captchaToken);

      const resp = await api.post('/users/login', formData.toString(), {
        headers: { 'Content-Type': 'application/x-www-form-urlencoded' }
      });

      if (resp.status !== 200) {
        throw new Error('Credenciales inválidas');
      }

      // Save expiry so the proactive interceptor can refresh before tokens die
      // ACCESS_TOKEN_EXPIRE_MINUTES from backend env (default 30)
      const expiresInMinutes: number = resp.data?.expires_in_minutes ?? 30;
      TokenManager.setExpiry(expiresInMinutes);

      navigate('/dashboard');
    } catch (err: any) {
      const serverFeedback = err.response?.data?.detail || err.response?.data?.errors?.[0]?.message;
      setError(serverFeedback || err.message);
      recaptchaRef.current?.reset();
      setCaptchaToken(null);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="flex items-center justify-center min-h-screen bg-background p-4 font-sans">
      <div className={`auth-container ${isRegisterActive ? 'active' : ''}`} id="container">

        {/* Sign Up Container */}
        <div className="form-container sign-up flex flex-col items-center justify-center text-center px-10">
          <form onSubmit={handleRegister} className="flex flex-col items-center justify-center h-full w-full">
            <img src={Logo} alt="ChefMate Logo" className="w-20 h-20 mb-4 object-contain drop-shadow-md" />
            <h1 className="text-3xl font-bold mb-4 text-text-primary font-heading">Crear Cuenta</h1>

            {error && isRegisterActive && <div className="text-red-500 text-sm mb-4">{error}</div>}

            <input
              type="text"
              placeholder="Nombre"
              className="input-field mt-4"
              value={registerForm.username}
              onChange={e => setRegisterForm({ ...registerForm, username: e.target.value })}
              required
            />
            <div className="w-full">
              <input
                type="email"
                placeholder="Correo Electrónico"
                className="input-field"
                value={registerForm.email}
                onChange={e => setRegisterForm({ ...registerForm, email: e.target.value })}
                required
              />
              {registerForm.email && !validateEmail(registerForm.email) && <p className="text-xs text-danger mt-1 text-left">Correo inválido</p>}
            </div>
            <div className="w-full">
              <div className="relative w-full">
                <input
                  type={showPassword ? "text" : "password"}
                  placeholder="Contraseña"
                  className="input-field w-full pr-10"
                  style={{ margin: "8px 0" }}
                  value={registerForm.password}
                  onChange={e => setRegisterForm({ ...registerForm, password: e.target.value })}
                  required
                />
                <button
                  type="button"
                  onClick={() => setShowPassword(!showPassword)}
                  className="absolute right-3 top-1/2 -translate-y-1/2 text-gray-500 hover:text-gray-700 transition-colors"
                >
                  {showPassword ? <EyeOff size={20} /> : <Eye size={20} />}
                </button>
              </div>
              {getPasswordFeedback(registerForm.password) && <p className="text-xs text-danger mt-1 text-left">{getPasswordFeedback(registerForm.password)}</p>}
              {renderStrengthMeter(registerForm.password)}
            </div>
            <button
              type="submit"
              className={`action-btn ${(!isRegisterValid || loading) ? 'opacity-50 cursor-not-allowed' : ''}`}
              disabled={loading || !isRegisterValid}
            >
              {loading ? 'REGISTRANDO...' : 'REGISTRARSE'}
            </button>
          </form>
        </div>

        {/* Sign In Container */}
        <div className="form-container sign-in flex flex-col items-center justify-center text-center px-10">
          <form onSubmit={handleLogin} className="flex flex-col items-center justify-center h-full w-full">
            <img src={Logo} alt="ChefMate Logo" className="w-20 h-20 mb-4 object-contain drop-shadow-md" />
            <h1 className="text-3xl font-bold mb-4 text-text-primary font-heading">Iniciar Sesión</h1>

            {error && !isRegisterActive && <div className="text-red-500 text-sm mb-4">{error}</div>}

            <div className="w-full">
              <input
                type="email"
                placeholder="Correo Electrónico"
                className="input-field mt-4"
                value={loginForm.username}
                onChange={e => setLoginForm({ ...loginForm, username: e.target.value })}
                required
              />
              {loginForm.username && !validateEmail(loginForm.username) && <p className="text-xs text-danger mt-1 text-left">Correo inválido</p>}
            </div>
            <div className="w-full relative">
              <input
                type={showPassword ? "text" : "password"}
                placeholder="Contraseña"
                className="input-field w-full pr-10"
                style={{ margin: "8px 0" }}
                value={loginForm.password}
                onChange={e => setLoginForm({ ...loginForm, password: e.target.value })}
                required
              />
              <button
                type="button"
                onClick={() => setShowPassword(!showPassword)}
                className="absolute right-3 top-1/2 -translate-y-1/2 text-gray-500 hover:text-gray-700 transition-colors"
              >
                {showPassword ? <EyeOff size={20} /> : <Eye size={20} />}
              </button>
            </div>
            <ReCAPTCHA ref={recaptchaRef} sitekey={RECAPTCHA_SITE_KEY} onChange={setCaptchaToken} className="mb-4" />
            <button
              type="submit"
              className={`action-btn ${(!isLoginValid || loading) ? 'opacity-50 cursor-not-allowed' : ''}`}
              disabled={loading || !isLoginValid}
            >
              {loading ? 'INICIANDO...' : 'INICIAR SESIÓN'}
            </button>
          </form>
        </div>

        {/* Toggle Panel (The overlay) */}
        <div className="toggle-container">
          <div className="toggle">
            {/* Left overlay (Visible when register is active) */}
            <div className="toggle-panel toggle-left">
              <h1 className="text-3xl font-bold text-white mb-4 font-heading">¡Bienvenido de nuevo!</h1>
              <p className="text-sm text-white/90 mb-8 max-w-[250px]">Ingresa tus datos para entrar a ChefMate</p>
              <button type="button" className="hidden-btn" id="login" onClick={togglePanel}>INICIAR SESIÓN</button>
            </div>

            {/* Right overlay (Visible when sign in is active) */}
            <div className="toggle-panel toggle-right">
              <h1 className="text-3xl font-bold text-white mb-4 font-heading">¡Hola, amigo!</h1>
              <p className="text-sm text-white/90 mb-8 max-w-[250px]">Regístrate para crear una cuenta en ChefMate</p>
              <button type="button" className="hidden-btn" id="register" onClick={togglePanel}>REGISTRARSE</button>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}
