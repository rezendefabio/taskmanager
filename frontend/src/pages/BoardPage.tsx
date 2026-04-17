import { useEffect, useState } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import api from '../api';

interface Task {
    id: number;
    title: string;
    description: string;
    status: string;
    priority: string;
    assigneeName: string | null;
    deadline: string | null;
}

const priorityColor: Record<string, string> = {
    LOW: '#4caf50',
    MEDIUM: '#ff9800',
    HIGH: '#f44336',
    CRITICAL: '#9c27b0',
};

export default function BoardPage() {
    const { projectId } = useParams();
    const navigate = useNavigate();
    const [tasks, setTasks] = useState<Task[]>([]);
    const [title, setTitle] = useState('');
    const [description, setDescription] = useState('');
    const [priority, setPriority] = useState('MEDIUM');
    const [showForm, setShowForm] = useState(false);

    useEffect(() => {
        loadTasks();
    }, []);

    const loadTasks = async () => {
        try {
            const res = await api.get(`/projects/${projectId}/tasks?size=100`);
            setTasks(res.data.content);
        } catch {
            navigate('/projects');
        }
    };

    const handleCreate = async (e: React.FormEvent) => {
        e.preventDefault();
        await api.post(`/projects/${projectId}/tasks`, { title, description, priority });
        setTitle('');
        setDescription('');
        setPriority('MEDIUM');
        setShowForm(false);
        loadTasks();
    };

    const changeStatus = async (taskId: number, newStatus: string) => {
        try {
            await api.patch(`/projects/${projectId}/tasks/${taskId}/status`, `"${newStatus}"`, {
                headers: { 'Content-Type': 'application/json' }
            });
            loadTasks();
        } catch (err: any) {
            const detail = err.response?.data?.detail || 'Erro ao mudar status';
            alert(detail);
        }
    };

    const columns = [
        { status: 'TODO', label: 'A Fazer', color: '#e3f2fd' },
        { status: 'IN_PROGRESS', label: 'Em Progresso', color: '#fff3e0' },
        { status: 'DONE', label: 'Concluido', color: '#e8f5e9' },
    ];

    const getNextStatuses = (current: string): { label: string; status: string }[] => {
        switch (current) {
            case 'TODO': return [{ label: 'Iniciar', status: 'IN_PROGRESS' }];
            case 'IN_PROGRESS': return [
                { label: 'Concluir', status: 'DONE' },
                { label: 'Voltar', status: 'TODO' },
            ];
            case 'DONE': return [{ label: 'Reabrir', status: 'IN_PROGRESS' }];
            default: return [];
        }
    };

    return (
        <div style={{ padding: 20 }}>
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 20 }}>
                <div>
                    <button onClick={() => navigate('/projects')} style={{ marginRight: 12 }}>Voltar</button>
                    <strong>Projeto #{projectId}</strong>
                </div>
                <button onClick={() => setShowForm(!showForm)} style={{ padding: '8px 16px' }}>
                    {showForm ? 'Cancelar' : 'Nova Tarefa'}
                </button>
            </div>

            {showForm && (
                <form onSubmit={handleCreate} style={{ marginBottom: 20, padding: 16, border: '1px solid #ccc', borderRadius: 8 }}>
                    <div style={{ marginBottom: 8 }}>
                        <input
                            placeholder="Titulo da tarefa"
                            value={title}
                            onChange={e => setTitle(e.target.value)}
                            required
                            style={{ width: '100%', padding: 8 }}
                        />
                    </div>
                    <div style={{ marginBottom: 8 }}>
                        <input
                            placeholder="Descricao"
                            value={description}
                            onChange={e => setDescription(e.target.value)}
                            style={{ width: '100%', padding: 8 }}
                        />
                    </div>
                    <div style={{ marginBottom: 8 }}>
                        <select value={priority} onChange={e => setPriority(e.target.value)} style={{ padding: 8 }}>
                            <option value="LOW">Baixa</option>
                            <option value="MEDIUM">Media</option>
                            <option value="HIGH">Alta</option>
                            <option value="CRITICAL">Critica</option>
                        </select>
                    </div>
                    <button type="submit" style={{ padding: '8px 16px' }}>Criar</button>
                </form>
            )}

            <div style={{ display: 'flex', gap: 16 }}>
                {columns.map(col => (
                    <div key={col.status} style={{
                        flex: 1,
                        backgroundColor: col.color,
                        borderRadius: 8,
                        padding: 12,
                        minHeight: 400,
                    }}>
                        <h3 style={{ textAlign: 'center', marginTop: 0 }}>
                            {col.label} ({tasks.filter(t => t.status === col.status).length})
                        </h3>
                        {tasks
                            .filter(t => t.status === col.status)
                            .map(task => (
                                <div key={task.id} style={{
                                    backgroundColor: '#fff',
                                    borderRadius: 6,
                                    padding: 12,
                                    marginBottom: 8,
                                    boxShadow: '0 1px 3px rgba(0,0,0,0.1)',
                                }}>
                                    <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                                        <strong>{task.title}</strong>
                                        <span style={{
                                            backgroundColor: priorityColor[task.priority],
                                            color: '#fff',
                                            padding: '2px 8px',
                                            borderRadius: 4,
                                            fontSize: 11,
                                        }}>
                      {task.priority}
                    </span>
                                    </div>
                                    {task.description && (
                                        <p style={{ margin: '4px 0', fontSize: 13, color: '#666' }}>{task.description}</p>
                                    )}
                                    {task.assigneeName && (
                                        <small style={{ color: '#999' }}>Responsavel: {task.assigneeName}</small>
                                    )}
                                    <div style={{ marginTop: 8, display: 'flex', gap: 4 }}>
                                        {getNextStatuses(task.status).map(action => (
                                            <button
                                                key={action.status}
                                                onClick={() => changeStatus(task.id, action.status)}
                                                style={{ fontSize: 11, padding: '4px 8px' }}
                                            >
                                                {action.label}
                                            </button>
                                        ))}
                                    </div>
                                </div>
                            ))}
                    </div>
                ))}
            </div>
        </div>
    );
}
