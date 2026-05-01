import React, { useState, useEffect, useCallback } from 'react';
import { api } from '../../services/api';
import { BookOpen, ChefHat, Clock, Users, Loader2, CookingPot, ChevronDown, ChevronUp, RefreshCw, AlertTriangle } from 'lucide-react';
import type { Recipe } from './types';

export default function RecipeHistory() {
    const [recipes, setRecipes] = useState<Recipe[]>([]);
    const [loading, setLoading] = useState(true);
    const [expandedId, setExpandedId] = useState<string | null>(null);
    const [cookingId, setCookingId] = useState<string | null>(null);
    const [servingsMap, setServingsMap] = useState<Record<string, number>>({});
    const [cookSuccessMap, setCookSuccessMap] = useState<Record<string, boolean>>({});

    const fetchHistory = useCallback(async () => {
        setLoading(true);
        try {
            const res = await api.get('/recipes');
            const list: Recipe[] = Array.isArray(res.data) ? res.data : [];
            setRecipes(list);
            const initial: Record<string, number> = {};
            list.forEach(r => { if (r.id) initial[r.id] = 1; });
            setServingsMap(initial);
        } catch (e) {
            console.error('Error loading recipe history', e);
        } finally {
            setLoading(false);
        }
    }, []);

    useEffect(() => { fetchHistory(); }, [fetchHistory]);

    const handleCook = async (recipe: Recipe) => {
        if (!recipe.id) return;
        const servings = servingsMap[recipe.id] || 1;
        setCookingId(recipe.id);
        try {
            await api.post(`/recipes/${recipe.id}/cook`, { servings });
            setCookSuccessMap(prev => ({ ...prev, [recipe.id!]: true }));
            setTimeout(() => {
                setCookSuccessMap(prev => { const n = { ...prev }; delete n[recipe.id!]; return n; });
                fetchHistory();
            }, 2000);
        } catch (e: any) {
            alert(e?.response?.data?.detail || 'Error al cocinar.');
        } finally {
            setCookingId(null);
        }
    };

    const getAvailabilityStyle = (max: number) => {
        if (max === 0) return { bar: 'bg-danger', text: 'text-danger', label: 'Sin stock' };
        if (max <= 2) return { bar: 'bg-warning', text: 'text-warning', label: `${max} porción(es)` };
        return { bar: 'bg-primary', text: 'text-primary', label: `${max} porción(es)` };
    };

    return (
        <div className="w-full">
            {/* Header */}
            <div className="flex justify-between items-end mb-8">
                <div>
                    <h1 className="text-4xl font-heading font-bold text-text-primary mb-2">Historial de Recetas</h1>
                    <p className="text-text-secondary">Recetas guardadas. Cocínalas de nuevo según el stock disponible.</p>
                </div>
                <button
                    onClick={fetchHistory}
                    disabled={loading}
                    className="flex items-center gap-2 px-5 py-3 rounded-xl border border-border-medium text-text-secondary hover:border-primary hover:text-primary bg-surface font-semibold transition-all"
                >
                    <RefreshCw size={16} className={loading ? 'animate-spin' : ''} />
                    Actualizar
                </button>
            </div>

            {/* Loading */}
            {loading && (
                <div className="flex justify-center py-20 text-text-muted gap-3">
                    <Loader2 size={22} className="animate-spin" />
                    Cargando historial…
                </div>
            )}

            {/* Empty */}
            {!loading && recipes.length === 0 && (
                <div className="flex flex-col items-center justify-center py-24 gap-4 text-text-muted">
                    <BookOpen size={64} strokeWidth={1} className="text-border-medium" />
                    <p className="text-lg">Aún no has guardado ninguna receta.</p>
                    <p className="text-sm">Genera recetas con el Chef IA y presiona &quot;Cocinar&quot;.</p>
                </div>
            )}

            {/* Recipe list */}
            <div className="flex flex-col gap-5">
                {recipes.map(recipe => {
                    if (!recipe.id) return null;
                    const max = recipe.max_servings ?? 0;
                    const avail = getAvailabilityStyle(max);
                    const isExpanded = expandedId === recipe.id;
                    const isCooking = cookingId === recipe.id;
                    const isSuccess = cookSuccessMap[recipe.id];
                    const selectedServings = servingsMap[recipe.id] || 1;
                    const canCook = max > 0;

                    return (
                        <div
                            key={recipe.id}
                            className={`bg-surface rounded-2xl border overflow-hidden shadow-sm transition-all duration-300 ${max === 0 ? 'border-border-light opacity-70' : 'border-border-light hover:border-primary/30 hover:shadow-md'}`}
                        >
                            <div className="p-6">
                                <div className="flex items-start gap-4">
                                    {/* Icon */}
                                    <div className={`p-3 rounded-xl shrink-0 ${recipe.type === 'reposteria' ? 'bg-accent-light text-accent' : 'bg-primary-light text-primary'}`}>
                                        <ChefHat size={22} strokeWidth={2} />
                                    </div>

                                    {/* Info */}
                                    <div className="flex-1 min-w-0">
                                        <div className="flex items-start justify-between gap-3">
                                            <div>
                                                <h3 className="text-xl font-bold text-text-primary leading-tight">{recipe.name}</h3>
                                                <div className="flex items-center gap-3 mt-1">
                                                    <span className={`text-xs font-semibold px-2 py-0.5 rounded-full ${recipe.type === 'reposteria' ? 'bg-accent-light text-accent' : 'bg-primary-light text-primary'}`}>
                                                        {recipe.type || 'plato'}
                                                    </span>
                                                    <span className="flex items-center gap-1 text-xs text-text-muted">
                                                        <Clock size={12} /> {recipe.prep_time_minutes} min
                                                    </span>
                                                    {recipe.created_at && (
                                                        <span className="text-xs text-text-muted">
                                                            {new Date(recipe.created_at).toLocaleDateString('es-BO')}
                                                        </span>
                                                    )}
                                                </div>
                                            </div>

                                            {/* Stock availability pill */}
                                            <div className={`flex items-center gap-1.5 px-3 py-1.5 rounded-full border shrink-0 ${max === 0 ? 'border-danger/30 bg-danger/5' : max <= 2 ? 'border-warning/30 bg-warning/5' : 'border-primary/30 bg-primary/5'}`}>
                                                {max === 0 && <AlertTriangle size={13} className="text-danger" />}
                                                <Users size={13} className={avail.text} />
                                                <span className={`text-xs font-bold ${avail.text}`}>{avail.label}</span>
                                            </div>
                                        </div>

                                        <p className="text-sm text-text-secondary mt-2 line-clamp-2">{recipe.description}</p>

                                        {/* Stock progress bar */}
                                        <div className="mt-3">
                                            <div className="h-1.5 bg-surface-secondary rounded-full overflow-hidden">
                                                <div
                                                    className={`h-full rounded-full transition-all duration-500 ${avail.bar}`}
                                                    style={{ width: max === 0 ? '0%' : `${Math.min(100, (max / 10) * 100)}%` }}
                                                />
                                            </div>
                                        </div>
                                    </div>
                                </div>

                                {/* Actions */}
                                <div className="flex items-center gap-3 mt-5">
                                    <button
                                        onClick={() => setExpandedId(isExpanded ? null : (recipe.id ?? null))}
                                        className="flex items-center gap-1.5 px-4 py-2.5 rounded-xl border border-border-medium text-text-secondary hover:border-primary hover:text-primary transition-all text-sm font-semibold"
                                    >
                                        {isExpanded ? <ChevronUp size={15} /> : <ChevronDown size={15} />}
                                        {isExpanded ? 'Ocultar' : 'Ver receta'}
                                    </button>

                                    {/* Servings selector */}
                                    <div className="flex items-center gap-2 ml-auto">
                                        <label className="text-xs text-text-muted font-medium flex items-center gap-1">
                                            <Users size={13} /> Porciones:
                                        </label>
                                        <input
                                            type="number"
                                            min={1}
                                            max={max || 1}
                                            value={selectedServings}
                                            disabled={!canCook}
                                            onChange={e => setServingsMap(prev => ({ ...prev, [recipe.id!]: Math.max(1, Math.min(max, parseInt(e.target.value) || 1)) }))}
                                            className="w-16 text-center bg-background border border-border-medium rounded-lg px-2 py-1.5 text-sm font-bold outline-none disabled:opacity-40"
                                        />
                                    </div>

                                    <button
                                        id={`cook-history-${recipe.id}`}
                                        onClick={() => handleCook(recipe)}
                                        disabled={!canCook || isCooking || isSuccess}
                                        title={!canCook ? 'Sin stock suficiente para cocinar' : undefined}
                                        className={`flex items-center gap-2 px-5 py-2.5 rounded-xl font-bold text-sm transition-all ${
                                            isSuccess
                                                ? 'bg-primary-light text-primary cursor-default'
                                                : canCook
                                                ? 'bg-primary hover:bg-primary-hover text-white shadow-lg shadow-primary/20 hover:scale-[1.02] active:scale-[0.98]'
                                                : 'bg-surface-secondary text-text-muted cursor-not-allowed border border-border-light'
                                        }`}
                                    >
                                        {isCooking ? (
                                            <><Loader2 size={16} className="animate-spin" /> Cocinando…</>
                                        ) : isSuccess ? (
                                            <>✓ ¡Listo!</>
                                        ) : (
                                            <><CookingPot size={16} /> Cocinar</>
                                        )}
                                    </button>
                                </div>
                            </div>

                            {/* Expanded */}
                            {isExpanded && (
                                <div className="border-t border-border-light p-6 bg-background animate-in fade-in slide-in-from-top-1 duration-200">
                                    <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
                                        <div>
                                            <h4 className="text-sm font-bold text-text-primary uppercase tracking-wider mb-3">Ingredientes <span className="text-text-muted font-normal">(por porción)</span></h4>
                                            <ul className="space-y-2">
                                                {recipe.ingredients.map((ing, i) => (
                                                    <li key={i} className="flex justify-between items-center text-sm py-2 border-b border-border-light last:border-0">
                                                        <span className="text-text-primary font-medium">{ing.name}</span>
                                                        <div className="text-right">
                                                            <span className="text-text-secondary font-mono">{ing.quantity} {ing.unit}</span>
                                                            {selectedServings > 1 && (
                                                                <span className="block text-xs text-text-muted">
                                                                    × {selectedServings} = {(ing.quantity * selectedServings).toFixed(2)} {ing.unit}
                                                                </span>
                                                            )}
                                                        </div>
                                                    </li>
                                                ))}
                                            </ul>
                                        </div>
                                        <div>
                                            <h4 className="text-sm font-bold text-text-primary uppercase tracking-wider mb-3">Pasos</h4>
                                            <ol className="space-y-3">
                                                {recipe.steps.map((step, i) => (
                                                    <li key={i} className="flex gap-3 text-sm text-text-secondary">
                                                        <span className="flex-shrink-0 w-6 h-6 rounded-full bg-primary-light text-primary font-bold text-xs flex items-center justify-center">{i + 1}</span>
                                                        <span>{step}</span>
                                                    </li>
                                                ))}
                                            </ol>
                                        </div>
                                    </div>
                                </div>
                            )}
                        </div>
                    );
                })}
            </div>
        </div>
    );
}
