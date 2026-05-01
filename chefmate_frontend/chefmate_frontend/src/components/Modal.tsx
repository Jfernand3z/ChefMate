import React from 'react';
import { X } from 'lucide-react';

interface ModalProps {
    isOpen: boolean;
    onClose: () => void;
    title: string;
    children: React.ReactNode;
}

export default function Modal({ isOpen, onClose, title, children }: ModalProps) {
    if (!isOpen) return null;

    return (
        <div className="fixed inset-0 z-[100] flex items-center justify-center bg-text-primary/40 backdrop-blur-sm p-4">
            <div className="bg-surface rounded-3xl p-8 w-full max-w-md shadow-2xl relative animate-in fade-in zoom-in duration-200">
                <button 
                    onClick={onClose} 
                    className="absolute top-6 right-6 text-text-muted hover:text-danger transition-colors cursor-pointer"
                >
                    <X size={24} />
                </button>
                
                <h2 className="text-2xl font-bold font-heading mb-6">{title}</h2>
                
                {children}
            </div>
        </div>
    );
}
