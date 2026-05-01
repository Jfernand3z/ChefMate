import React, { useState, useEffect } from 'react';
import { api } from '../../services/api';
import { Plus, Edit2, Trash2, AlertTriangle, CheckCircle2, Clock } from 'lucide-react';
import Modal from '../../components/Modal';

interface Product {
    id?: string;
    name: string;
    quantity: number;
    unit: string;
    expiration_date?: string;
    category: string;
}

export default function ProductManager() {
    const [products, setProducts] = useState<Product[]>([]);
    const [loading, setLoading] = useState(true);
    const [showModal, setShowModal] = useState(false);
    
    const [formData, setFormData] = useState<any>({
        id: '',
        name: '',
        quantity: 1,
        unit: 'Kg',
        expiration_date: '',
        category: 'Especias'
    });

    const fetchProducts = async () => {
        setLoading(true);
        try {
            const res = await api.get('/products');
            const fetchedProducts = res.data.products || res.data || [];
            setProducts(fetchedProducts);
        } catch (e) {
            console.error("Failed to load products", e);
        } finally {
            setLoading(false);
        }
    };

    useEffect(() => {
        fetchProducts();
    }, []);

    const handleSave = async (e: React.FormEvent) => {
        e.preventDefault();
        try {
            const payload = { ...formData };
            if (!payload.expiration_date) payload.expiration_date = null; // Send null to Pydantic

            if (formData.id) {
                await api.put(`/products/${formData.id}`, payload);
            } else {
                delete payload.id;
                await api.post('/products', payload);
            }
            setShowModal(false);
            fetchProducts();
        } catch (e) {
            alert('Error al guardar el producto');
        }
    };

    const handleEdit = (p: Product) => {
        setFormData({
            id: p.id || '',
            name: p.name,
            quantity: p.quantity,
            unit: p.unit,
            expiration_date: p.expiration_date ? p.expiration_date.split('T')[0] : '',
            category: p.category || ''
        });
        setShowModal(true);
    };

    const handleDelete = async (id: string) => {
        if (!confirm('¿Eliminar producto de forma permanente?')) return;
        try {
            await api.delete(`/products/${id}`);
            fetchProducts();
        } catch (e) {
            alert('Error eliminando producto');
        }
    };

    const openCreateModal = () => {
        setFormData({ id: '', name: '', quantity: 1, unit: 'Kg', expiration_date: '', category: 'General' });
        setShowModal(true);
    };

    const getExpirationStyles = (dateStr?: string) => {
        if (!dateStr) return {
            text: 'Sin fecha',
            diffDays: null,
            cardClasses: 'border-border-light hover:border-border-medium',
            badgeClasses: 'bg-surface-secondary text-text-muted',
            textClasses: 'text-text-muted',
            icon: null,
            urgencyWidth: '0%',
            urgencyColor: 'bg-border-medium',
        };

        const expDate = new Date(dateStr);
        const today = new Date();
        today.setHours(0, 0, 0, 0);
        const diffTime = expDate.getTime() - today.getTime();
        const diffDays = Math.ceil(diffTime / (1000 * 60 * 60 * 24));
        const formattedText = dateStr.split('T')[0];

        if (diffDays < 0) {
            return {
                text: formattedText,
                diffDays,
                cardClasses: 'border-red-400 bg-red-50 shadow-md shadow-red-200',
                badgeClasses: 'bg-red-500 text-white',
                textClasses: 'text-red-600 font-bold',
                icon: 'expired',
                urgencyWidth: '100%',
                urgencyColor: 'bg-red-500',
            };
        }
        if (diffDays <= 3) {
            return {
                text: formattedText,
                diffDays,
                cardClasses: 'border-orange-400 bg-orange-50 shadow-md shadow-orange-200',
                badgeClasses: 'bg-orange-500 text-white',
                textClasses: 'text-orange-600 font-bold',
                icon: 'critical',
                urgencyWidth: '80%',
                urgencyColor: 'bg-orange-500',
            };
        }
        if (diffDays <= 7) {
            return {
                text: formattedText,
                diffDays,
                cardClasses: 'border-amber-300 bg-amber-50 shadow-sm shadow-amber-100',
                badgeClasses: 'bg-amber-400 text-white',
                textClasses: 'text-amber-600 font-bold',
                icon: 'warning',
                urgencyWidth: '50%',
                urgencyColor: 'bg-amber-400',
            };
        }
        return {
            text: formattedText,
            diffDays,
            cardClasses: 'border-border-light hover:border-primary/40 hover:shadow-sm',
            badgeClasses: 'bg-primary-light text-primary',
            textClasses: 'text-primary font-semibold',
            icon: 'ok',
            urgencyWidth: '15%',
            urgencyColor: 'bg-primary',
        };
    };

    const getMinDate = () => {
        const todayStr = new Date().toLocaleDateString('en-CA'); // 'YYYY-MM-DD' logically based on local timezone
        // If editing an existing product with a past date, allow that past date as the minimum
        if (formData.id && formData.expiration_date && formData.expiration_date < todayStr) {
            return formData.expiration_date;
        }
        return todayStr;
    };

    return (
        <div className="w-full relative">
            <div className="flex justify-between items-end mb-8 relative z-10">
                <div>
                    <h1 className="text-4xl font-heading font-bold text-text-primary mb-2">Inventario de Productos</h1>
                    <p className="text-text-secondary">Administra los ingredientes y vigila las caducidades.</p>
                </div>
                <button onClick={openCreateModal} className="bg-primary hover:bg-primary-hover text-white flex items-center gap-2 px-6 py-3 rounded-xl font-semibold shadow-lg shadow-primary/30 transition-all hover:scale-105 active:scale-95">
                    <Plus size={20} /> Nuevo Producto
                </button>
            </div>

            {loading ? (
                <div className="flex justify-center py-20 text-text-muted">Cargando inventario...</div>
            ) : (
                <div className="grid grid-cols-1 md:grid-cols-2 xl:grid-cols-3 gap-6 relative z-10">
                    {products.length === 0 && <p className="col-span-full text-text-muted text-center py-10">No hay productos. Agrega el primero.</p>}
                    
                    {products.map(p => {
                        const s = getExpirationStyles(p.expiration_date);
                        return (
                        <div key={p.id} className={`bg-surface rounded-2xl p-6 border-2 transition-all group relative overflow-hidden ${s.cardClasses}`}>

                            {/* Urgency bar at top */}
                            <div className="absolute top-0 left-0 right-0 h-1 bg-border-light rounded-t-2xl overflow-hidden">
                                <div className={`h-full rounded-full transition-all ${s.urgencyColor}`} style={{ width: s.urgencyWidth }} />
                            </div>

                            <div className="flex justify-between items-start mb-4 mt-1">
                                <div className="p-3 bg-surface-secondary rounded-xl text-primary font-bold text-xl uppercase tracking-wider shadow-sm">
                                    {p.name.substring(0, 2)}
                                </div>
                                <div className="flex gap-2">
                                    <button onClick={() => handleEdit(p)} className="p-2 text-text-muted hover:text-primary transition-colors hover:bg-primary-light rounded-lg">
                                        <Edit2 size={18} />
                                    </button>
                                    <button onClick={() => handleDelete(p.id || '')} className="p-2 text-text-muted hover:text-danger transition-colors hover:bg-danger-light rounded-lg">
                                        <Trash2 size={18} />
                                    </button>
                                </div>
                            </div>

                            <h3 className="text-xl font-bold text-text-primary mb-1 truncate">{p.name}</h3>
                            <span className="text-xs font-semibold px-2 py-1 bg-accent-light text-accent rounded-full inline-block mb-4">
                                {p.category || 'Sin Categoría'}
                            </span>

                            <div className="grid grid-cols-2 gap-4 mt-2 pt-4 border-t border-dashed border-border-light">
                                <div>
                                    <p className="text-xs text-text-muted mb-1">Cantidad</p>
                                    <p className="font-bold text-text-primary">{p.quantity} <span className="text-sm font-normal text-text-secondary">{p.unit}</span></p>
                                </div>
                                <div>
                                    <p className="text-xs text-text-muted mb-1">Vencimiento</p>
                                    {p.expiration_date ? (
                                        <div className="flex items-center gap-1.5">
                                            {s.icon === 'expired' && <AlertTriangle size={14} className="text-red-500 shrink-0" />}
                                            {s.icon === 'critical' && <AlertTriangle size={14} className="text-orange-500 shrink-0" />}
                                            {s.icon === 'warning' && <Clock size={14} className="text-amber-500 shrink-0" />}
                                            {s.icon === 'ok' && <CheckCircle2 size={14} className="text-primary shrink-0" />}
                                            <div>
                                                <p className={`text-sm leading-tight ${s.textClasses}`}>{s.text}</p>
                                                {s.diffDays !== null && (
                                                    <span className={`text-xs font-bold px-1.5 py-0.5 rounded-md inline-block mt-0.5 ${s.badgeClasses}`}>
                                                        {s.diffDays < 0
                                                            ? `Venció hace ${Math.abs(s.diffDays)}d`
                                                            : s.diffDays === 0
                                                            ? '¡Vence hoy!'
                                                            : `${s.diffDays}d restantes`}
                                                    </span>
                                                )}
                                            </div>
                                        </div>
                                    ) : (
                                        <p className="text-sm text-text-muted">Sin fecha</p>
                                    )}
                                </div>
                            </div>
                        </div>
                        );
                    })}
                </div>
            )}

            {/* Reusable Modal Component */}
            <Modal
                isOpen={showModal}
                onClose={() => setShowModal(false)}
                title={formData.id ? 'Editar Producto' : 'Crear Producto'}
            >
                <form onSubmit={handleSave} className="flex flex-col gap-4">
                    <div>
                        <label className="text-xs font-bold text-text-secondary uppercase tracking-wider mb-1 block">Nombre</label>
                        <input type="text" required value={formData.name} onChange={e => setFormData({...formData, name: e.target.value})} className="w-full bg-background border border-border-medium focus:border-primary px-4 py-3 rounded-xl outline-none transition-colors" />
                    </div>
                    
                    <div className="grid grid-cols-2 gap-4">
                        <div>
                            <label className="text-xs font-bold text-text-secondary uppercase tracking-wider mb-1 block">Cantidad</label>
                            <input type="number" step="0.01" min="0.1" required value={formData.quantity} onChange={e => setFormData({...formData, quantity: parseFloat(e.target.value)})} className="w-full bg-background border border-border-medium focus:border-primary px-4 py-3 rounded-xl outline-none transition-colors" />
                        </div>
                        <div>
                            <label className="text-xs font-bold text-text-secondary uppercase tracking-wider mb-1 block">Unidad</label>
                            <select value={formData.unit} onChange={e => setFormData({...formData, unit: e.target.value})} className="w-full bg-background border border-border-medium focus:border-primary px-4 py-3 rounded-xl outline-none transition-colors">
                                <option value="Kg">Kg</option>
                                <option value="Gr">Gramos</option>
                                <option value="Litro">Litro</option>
                                <option value="Pieza">Pieza</option>
                            </select>
                        </div>
                    </div>
                    
                    <div className="grid grid-cols-2 gap-4">
                        <div>
                            <label className="text-xs font-bold text-text-secondary uppercase tracking-wider mb-1 block">Vencimiento <span className="lowercase text-text-muted/60 font-normal">(opcional)</span></label>
                            <input type="date" min={getMinDate()} value={formData.expiration_date} onChange={e => setFormData({...formData, expiration_date: e.target.value})} className="w-full bg-background border border-border-medium focus:border-primary px-4 py-3 rounded-xl outline-none transition-colors" />
                        </div>
                        <div>
                            <label className="text-xs font-bold text-text-secondary uppercase tracking-wider mb-1 block">Categoría</label>
                            <input type="text" required value={formData.category} onChange={e => setFormData({...formData, category: e.target.value})} className="w-full bg-background border border-border-medium focus:border-primary px-4 py-3 rounded-xl outline-none transition-colors" />
                        </div>
                    </div>

                    <button type="submit" className="mt-4 w-full bg-primary hover:bg-primary-hover text-white py-4 rounded-xl font-bold uppercase tracking-wide transition-colors">
                        {formData.id ? 'Actualizar Producto' : 'Crear Producto'}
                    </button>
                </form>
            </Modal>
        </div>
    );
}
