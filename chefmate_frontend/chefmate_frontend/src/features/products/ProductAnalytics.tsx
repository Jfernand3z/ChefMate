import React, { useEffect, useState } from 'react';
import { api } from '../../services/api';
import { PieChart, Pie, Cell, Tooltip, Legend, ResponsiveContainer } from 'recharts';

export default function ProductAnalytics() {
    const [data, setData] = useState<any[]>([]);
    const [loading, setLoading] = useState(true);

    const COLORS = ['#10B981', '#F97316', '#3B82F6', '#F59E0B', '#8B5CF6'];

    useEffect(() => {
        api.get('/products/distribution/category')
            .then(res => setData(res.data))
            .catch(err => console.error("Error loading analytics", err))
            .finally(() => setLoading(false));
    }, []);

    return (
        <div className="w-full">
            <h1 className="text-4xl font-heading font-bold text-text-primary mb-8">Radar de Ingredientes</h1>
            
            <div className="bg-surface p-8 rounded-3xl shadow-sm border border-border-light h-[500px]">
                <h2 className="text-xl font-bold text-text-primary mb-6">Distribución por Categorías</h2>
                {loading ? (
                    <div className="flex justify-center items-center h-full">
                        <span className="text-text-muted">Cargando gráfico...</span>
                    </div>
                ) : data.length > 0 ? (
                    <ResponsiveContainer width="100%" height="80%">
                        <PieChart>
                            <Pie
                                data={data}
                                cx="50%"
                                cy="50%"
                                innerRadius={80}
                                outerRadius={140}
                                fill="#8884d8"
                                paddingAngle={5}
                                dataKey="value"
                                nameKey="name"
                                label
                            >
                                {data.map((entry, index) => (
                                    <Cell key={`cell-${index}`} fill={COLORS[index % COLORS.length]} />
                                ))}
                            </Pie>
                            <Tooltip />
                            <Legend />
                        </PieChart>
                    </ResponsiveContainer>
                ) : (
                    <div className="flex justify-center items-center h-full text-text-muted">
                        No hay suficientes datos registrados
                    </div>
                )}
            </div>
        </div>
    );
}
