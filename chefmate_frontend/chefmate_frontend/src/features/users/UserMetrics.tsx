import React, { useEffect, useState } from 'react';
import { api } from '../../services/api';
import { useNavigate } from 'react-router-dom';
import { User, Mail, Lock, Save, Trash2, ShieldCheck, History } from 'lucide-react';

export default function UserMetrics() {
    const navigate = useNavigate();
    const [userData, setUserData] = useState<any>(null);
    const [accessLogs, setAccessLogs] = useState<any>(null);
    const [loading, setLoading] = useState(true);

    const [form, setForm] = useState({
        username: '',
        email: '',
        password: ''
    });

    useEffect(() => {
        Promise.all([
            api.get('/auth/me'),
            api.get('/users/logs')
        ]).then(([userRes, logsRes]) => {
            setUserData(userRes.data);
            setAccessLogs(logsRes.data);
            
            // Auto-fill form
            setForm({
                username: userRes.data.username || '',
                email: userRes.data.email || '',
                password: '' // Don't auto-fill passwords
            });
        }).catch(err => {
            console.error("Error loading profile", err);
        }).finally(() => {
            setLoading(false);
        });
    }, []);

    const performLogout = async () => {
        try {
            await api.patch('/users/logout');
        } catch (e) {
            console.error(e);
        } finally {
            navigate('/');
        }
    };

    const handleUpdate = async (e: React.FormEvent) => {
        e.preventDefault();
        if (!userData?.id) return;

        try {
            const payload: any = {
                username: form.username,
                email: form.email
            };
            if (form.password.trim() !== '') {
                payload.password = form.password;
            }

            await api.put(`/users/${userData.id}`, payload);
            alert("Cuenta actualizada con éxito. Por seguridad debes iniciar sesión nuevamente.");
            await performLogout();
        } catch (err: any) {
            const msg = err.response?.data?.detail || err.response?.data?.errors?.[0]?.message || 'Error al actualizar';
            alert(msg);
        }
    };

    const handleDelete = async () => {
        if (!userData?.id) return;
        if (!confirm('¿Estás seguro de eliminar PERMANENTEMENTE tu cuenta y salir del sistema?')) return;

        try {
            await api.delete(`/users/${userData.id}`);
            alert("Cuenta eliminada con éxito.");
            await performLogout();
        } catch (err) {
            alert('Hubo un error al eliminar la cuenta');
        }
    };

    return (
        <div className="w-full">
            <h1 className="text-4xl font-heading font-bold text-text-primary mb-8">Centro de Miembro</h1>
            
            <div className="grid grid-cols-1 xl:grid-cols-2 gap-8">
                
                {/* Profile Form Block */}
                <div className="bg-surface p-8 rounded-3xl shadow-sm border border-border-light relative overflow-hidden">
                    <div className="absolute -top-10 -right-10 opacity-5 pointer-events-none">
                        <ShieldCheck size={200} />
                    </div>
                    
                    <h2 className="text-xl font-bold font-heading text-text-primary mb-6 flex items-center gap-2">
                        <User size={24} className="text-primary"/> Configuración de Cuenta
                    </h2>
                    
                    {loading ? (
                        <p className="text-text-muted">Cargando datos personales...</p>
                    ) : (
                        <form onSubmit={handleUpdate} className="space-y-5 relative z-10">
                            <div>
                                <label className="text-xs font-bold text-text-secondary uppercase tracking-wider mb-1 block">Nombre de Usuario</label>
                                <div className="relative">
                                    <User size={18} className="absolute left-4 top-1/2 -translate-y-1/2 text-text-muted" />
                                    <input 
                                        type="text" 
                                        required 
                                        value={form.username} 
                                        onChange={e => setForm({...form, username: e.target.value})} 
                                        className="w-full bg-background border border-border-medium focus:border-primary pl-12 pr-4 py-3 rounded-xl outline-none transition-colors" 
                                    />
                                </div>
                            </div>

                            <div>
                                <label className="text-xs font-bold text-text-secondary uppercase tracking-wider mb-1 block">Correo Electrónico</label>
                                <div className="relative">
                                    <Mail size={18} className="absolute left-4 top-1/2 -translate-y-1/2 text-text-muted" />
                                    <input 
                                        type="email" 
                                        required 
                                        value={form.email} 
                                        onChange={e => setForm({...form, email: e.target.value})} 
                                        className="w-full bg-background border border-border-medium focus:border-primary pl-12 pr-4 py-3 rounded-xl outline-none transition-colors" 
                                    />
                                </div>
                            </div>

                            <div>
                                <label className="text-xs font-bold text-text-secondary uppercase tracking-wider mb-1 block">Nueva Contraseña <span className="text-text-muted/50 font-normal lowercase">(Opcional)</span></label>
                                <div className="relative">
                                    <Lock size={18} className="absolute left-4 top-1/2 -translate-y-1/2 text-text-muted" />
                                    <input 
                                        type="password" 
                                        placeholder="Dejar vacío para conservar la actual"
                                        value={form.password} 
                                        onChange={e => setForm({...form, password: e.target.value})} 
                                        className="w-full bg-background border border-border-medium focus:border-primary pl-12 pr-4 py-3 rounded-xl outline-none transition-colors" 
                                    />
                                </div>
                            </div>

                            <div className="flex gap-4 pt-4 mt-8 border-t border-border-light border-dashed">
                                <button type="submit" className="flex-1 bg-primary hover:bg-primary-hover text-white flex items-center justify-center gap-2 py-3 rounded-xl font-bold transition-all shadow-lg shadow-primary/20 hover:scale-[1.02] active:scale-[0.98]">
                                    <Save size={18} /> Actualizar
                                </button>
                                <button type="button" onClick={handleDelete} className="bg-danger/10 hover:bg-danger text-danger hover:text-white flex items-center justify-center gap-2 px-6 py-3 rounded-xl font-bold transition-all">
                                    <Trash2 size={18} /> Borrar Cuenta
                                </button>
                            </div>
                        </form>
                    )}
                </div>

                {/* Logs Block */}
                <div className="bg-surface p-8 rounded-3xl shadow-sm border border-border-light relative overflow-hidden flex flex-col h-full">
                    <div className="absolute -bottom-10 -right-10 opacity-5 pointer-events-none">
                        <History size={200} />
                    </div>
                    
                    <h2 className="text-xl font-bold font-heading text-text-primary mb-6 flex items-center gap-2">
                        <History size={24} className="text-accent"/> Actividad Reciente
                    </h2>
                    
                    {loading ? (
                        <p className="text-text-muted flex-1">Verificando auditorías...</p>
                    ) : (
                        <div className="flex-1 overflow-y-auto pr-2 relative z-10 custom-scrollbar max-h-[400px]">
                            {accessLogs?.recent_activity?.length > 0 ? (
                                <ul className="space-y-4">
                                    {accessLogs.recent_activity.map((log: any, idx: number) => (
                                        <li key={idx} className="p-4 bg-background rounded-2xl border border-border-light flex flex-col hover:border-accent/40 transition-colors group">
                                            <div className="flex justify-between items-center mb-2">
                                                <span className="text-xs text-text-muted font-medium">{new Date(log.date).toLocaleString()}</span>
                                                <span className="text-xs font-bold px-3 py-1 bg-accent-light text-accent rounded-full border border-accent/20 group-hover:bg-accent group-hover:text-white transition-colors">{log.browser}</span>
                                            </div>
                                            <span className="text-sm font-semibold text-text-primary">{log.action}</span>
                                        </li>
                                    ))}
                                </ul>
                            ) : (
                                <div className="h-full flex flex-col items-center justify-center text-text-muted">
                                    <History size={48} className="opacity-20 mb-4" />
                                    <p>Haciendo seguimiento... no hay acción aún.</p>
                                </div>
                            )}
                        </div>
                    )}
                </div>

            </div>
        </div>
    );
}
