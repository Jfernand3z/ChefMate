import React, { useState, useEffect, useRef } from 'react';
import { PackageSearch, BarChart3, UserSquare2, LogOut, FileText, Download, X, ChefHat, BookOpen } from 'lucide-react';
import { api } from '../../services/api';
import { useNavigate } from 'react-router-dom';
import Logo from '../../assets/LogoChef.png';

interface LayoutProps {
  activeTab: string;
  setActiveTab: (tab: string) => void;
  children: React.ReactNode;
}

export default function Layout({ activeTab, setActiveTab, children }: LayoutProps) {
  const navigate = useNavigate();
  const [isLoggingOut, setIsLoggingOut] = useState(false);
  const [isLoadingPDF, setIsLoadingPDF] = useState(false);
  const [showPDFModal, setShowPDFModal] = useState(false);
  const [pdfObjectUrl, setPdfObjectUrl] = useState<string | null>(null);
  const objectUrlRef = useRef<string | null>(null);

  useEffect(() => {
    return () => {
      if (objectUrlRef.current) window.URL.revokeObjectURL(objectUrlRef.current);
    };
  }, []);

  const handleLogout = async () => {
    setIsLoggingOut(true);
    try {
      await api.patch('/users/logout');
    } catch (e) {
      console.error(e);
    } finally {
      setIsLoggingOut(false);
      navigate('/');
    }
  };

  const handleOpenPDFPreview = async () => {
    setIsLoadingPDF(true);
    try {
      const resp = await api.get('/products/report', { responseType: 'blob' });
      if (objectUrlRef.current) window.URL.revokeObjectURL(objectUrlRef.current);
      const url = window.URL.createObjectURL(new Blob([resp.data], { type: 'application/pdf' }));
      objectUrlRef.current = url;
      setPdfObjectUrl(url);
      setShowPDFModal(true);
    } catch (e) {
      console.error('Error cargando PDF', e);
      alert('Error al generar el informe PDF');
    } finally {
      setIsLoadingPDF(false);
    }
  };

  const handleDownloadPDF = () => {
    if (!pdfObjectUrl) return;
    const link = document.createElement('a');
    link.href = pdfObjectUrl;
    link.setAttribute('download', 'reporte_productos.pdf');
    document.body.appendChild(link);
    link.click();
    link.remove();
  };

  const handleClosePDFModal = () => {
    setShowPDFModal(false);
  };

  const menuItems = [
    { id: 'products', label: 'Mis Productos', icon: PackageSearch },
    { id: 'analytics', label: 'Radar de Recursos', icon: BarChart3 },
    { id: 'recipes', label: 'Chef IA', icon: ChefHat },
    { id: 'recipe-history', label: 'Historial Recetas', icon: BookOpen },
    { id: 'metrics', label: 'Mi Perfil & Logs', icon: UserSquare2 },
  ];

  return (
    <div className="flex h-screen bg-background font-sans overflow-hidden">

      {/* Floating Glass Sidebar */}
      <aside className="w-24 hover:w-72 transition-all duration-300 ease-in-out bg-surface/90 backdrop-blur-2xl border-r border-border-light shadow-2xl flex flex-col items-center py-8 z-50 fixed h-screen group">
        <div className="flex items-center gap-4 mb-12 w-full px-6 overflow-hidden">
          <img src={Logo} alt="ChefMate Logo" className="min-w-12 w-12 h-12 object-contain" />
          <span className="text-2xl font-heading font-extrabold text-text-primary whitespace-nowrap opacity-0 group-hover:opacity-100 transition-opacity duration-300">
            ChefMate
          </span>
        </div>

        <nav className="flex-1 w-full flex flex-col gap-2 px-4 overflow-hidden">
          {menuItems.map((item) => (
            <button
              key={item.id}
              onClick={() => setActiveTab(item.id)}
              className={`flex items-center gap-4 w-full p-4 rounded-2xl transition-all duration-300 ${activeTab === item.id
                  ? 'bg-primary text-white shadow-xl shadow-primary/30 scale-[1.02]'
                  : 'text-text-secondary hover:bg-surface-secondary hover:text-primary'
                }`}
            >
              <item.icon size={28} strokeWidth={2.5} className="min-w-8" />
              <span className={`font-semibold whitespace-nowrap opacity-0 group-hover:opacity-100 transition-opacity duration-300 ${activeTab === item.id ? 'text-white' : ''}`}>
                {item.label}
              </span>
            </button>
          ))}
        </nav>

        {/* Bottom Actions */}
        <div className="w-full px-4 overflow-hidden mt-auto flex flex-col gap-3">
          <button
            onClick={handleOpenPDFPreview}
            disabled={isLoadingPDF}
            className={`flex items-center gap-4 w-full p-4 rounded-2xl text-text-secondary hover:bg-info-light hover:text-info transition-all duration-300 ${isLoadingPDF ? 'opacity-50 cursor-not-allowed' : ''}`}
          >
            <FileText size={28} strokeWidth={2.5} className="min-w-8 text-info" />
            <span className="font-semibold whitespace-nowrap opacity-0 group-hover:opacity-100 transition-opacity duration-300">
              {isLoadingPDF ? 'Generando...' : 'Ver Reporte PDF'}
            </span>
          </button>

          <button
            onClick={handleLogout}
            disabled={isLoggingOut}
            className="flex items-center gap-4 w-full p-4 rounded-2xl bg-danger-light/50 text-danger hover:bg-danger hover:text-white transition-all duration-300 shadow-sm"
          >
            <LogOut size={28} strokeWidth={2} className="min-w-8" />
            <span className="font-semibold whitespace-nowrap opacity-0 group-hover:opacity-100 transition-opacity duration-300">
              {isLoggingOut ? 'Saliendo...' : 'Cerrar Sesión'}
            </span>
          </button>
        </div>
      </aside>

      {/* Main Content Area */}
      <main className="flex-1 ml-24 bg-background overflow-y-auto">
        <div className="max-w-[1400px] mx-auto min-h-screen p-8 md:p-12">
          {children}
        </div>
      </main>

      {/* PDF Preview Modal */}
      {showPDFModal && pdfObjectUrl && (
        <div
          className="fixed inset-0 z-[100] flex items-center justify-center bg-black/70 backdrop-blur-sm"
          onClick={handleClosePDFModal}
        >
          <div
            className="relative flex flex-col bg-surface rounded-3xl shadow-2xl border border-border-light overflow-hidden"
            style={{ width: '90vw', height: '90vh', maxWidth: '1100px' }}
            onClick={(e) => e.stopPropagation()}
          >
            <div className="flex items-center justify-between px-6 py-4 border-b border-border-light bg-surface-secondary shrink-0">
              <div className="flex items-center gap-3">
                <FileText size={22} className="text-primary" />
                <h2 className="text-lg font-bold text-text-primary font-heading">Reporte de Productos</h2>
              </div>
              <div className="flex items-center gap-3">
                <button
                  id="pdf-download-btn"
                  onClick={handleDownloadPDF}
                  className="flex items-center gap-2 px-5 py-2.5 bg-primary hover:bg-primary-hover text-white rounded-xl font-semibold text-sm shadow-lg shadow-primary/30 transition-all hover:scale-105 active:scale-95"
                >
                  <Download size={16} />
                  Descargar PDF
                </button>
                <button
                  id="pdf-close-btn"
                  onClick={handleClosePDFModal}
                  className="p-2.5 rounded-xl text-text-muted hover:text-text-primary hover:bg-surface transition-colors"
                >
                  <X size={20} />
                </button>
              </div>
            </div>

            <iframe
              src={pdfObjectUrl}
              title="Reporte PDF"
              className="flex-1 w-full border-0 bg-white"
            />
          </div>
        </div>
      )}
    </div>
  );
}
