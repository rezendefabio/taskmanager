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

interface Member {
    id: number;
    name: string;
}

interface Report {
    byStatus: Record<string, number>;
    byPriority: Record<string, number>;
}

const priorityColor: Record<string, string> = {
    LOW: '#4caf50',
    MEDIUM: '#ff9800',
    HIGH: '#f44336',
    CRITICAL: '#9c27b0',
};

const statusLabel: Record<string, string> = {
    TODO: 'A Fazer',
    IN_PROGRESS: 'Em Progresso',
    DONE: 'Concluido',
};

const priorityLabel: Record<string, string> = {
    LOW: 'Baixa',
    MEDIUM: 'Media',
    HIGH: 'Alta',
    CRITICAL: 'Critica',
};

export default function BoardPage() {
    const { projectId } = useParams();
    const navigate = useNavigate();
    const [tasks, setTasks] = useState<Task[]>([]);
    const [members, setMembers] = useState<Member[]>([]);
    const [title, setTitle] = useState('');
    const [description, setDescription] = useState('');
    const [priority, setPriority] = useState('MEDIUM');
    const [assigneeId, setAssigneeId] = useState('');
    const [showForm, setShowForm] = useState(false);
    const [editingTask, setEditingTask] = useState<Task | null>(null);
    const [editTitle, setEditTitle] = useState('');
    const [editDescription, setEditDescription] = useState('');
    const [assigningTaskId, setAssigningTaskId] = useState<number | null>(null);
    const [newAssigneeId, setNewAssigneeId] = useState('');
    const [report, setReport] = useState<Report | null>(null);
    const [showReport, setShowReport] = useState(false);
    const [projectName, setProjectName] = useState('');
    const [filterPriority, setFilterPriority] = useState('');
    const [filterAssignee, setFilterAssignee] = useState('');
    const [filterSearch, setFilterSearch] = useState('');
    const [showFilters, setShowFilters] = useState(false);

    useEffect(() => {
        loadTasks();
        loadProject();
    }, []);

    const loadTasks = async (priorityFilter?: string, assigneeFilter?: string, searchFilter?: string) => {
        try {
            const params = new URLSearchParams();
            params.append('size', '100');
            if (priorityFilter) params.append('priority', priorityFilter);
            if (assigneeFilter) params.append('assigneeId', assigneeFilter);
            if (searchFilter) params.append('search', searchFilter);

            const res = await api.get(`/projects/${projectId}/tasks?${params.toString()}`);
            setTasks(res.data.content);
        } catch {
            navigate('/projects');
        }
    };

    const applyFilters = () => {
        loadTasks(filterPriority, filterAssignee, filterSearch);
    };

    const clearFilters = () => {
        setFilterPriority('');
        setFilterAssignee('');
        setFilterSearch('');
        loadTasks();
    };

    const loadProject = async () => {
        try {
            const res = await api.get('/projects');
            const project = res.data.find((p: any) => p.id === Number(projectId));
            if (project) {
                setMembers(project.members);
                setProjectName(project.name);
            }
        } catch {}
    };

    const loadReport = async () => {
        try {
            const res = await api.get(`/projects/${projectId}/tasks/report`);
            setReport(res.data);
            setShowReport(true);
        } catch {
            alert('Erro ao carregar relatorio');
        }
    };

    const handleCreate = async (e: React.FormEvent) => {
        e.preventDefault();
        await api.post(`/projects/${projectId}/tasks`, {
            title, description, priority,
            assigneeId: assigneeId ? Number(assigneeId) : null
        });
        setTitle('');
        setDescription('');
        setPriority('MEDIUM');
        setAssigneeId('');
        setShowForm(false);
        loadTasks(filterPriority, filterAssignee, filterSearch);
    };

    const handleEdit = async (e: React.FormEvent) => {
        e.preventDefault();
        if (!editingTask) return;
        try {
            await api.put(`/projects/${projectId}/tasks/${editingTask.id}`, {
                title: editTitle,
                description: editDescription,
            });
            setEditingTask(null);
            loadTasks(filterPriority, filterAssignee, filterSearch);
        } catch (err: any) {
            alert(err.response?.data?.detail || 'Erro ao atualizar tarefa');
        }
    };

    const handleAssign = async (taskId: number) => {
        try {
            await api.patch(`/projects/${projectId}/tasks/${taskId}/assign`, {
                assigneeId: newAssigneeId ? Number(newAssigneeId) : null
            });
            setAssigningTaskId(null);
            setNewAssigneeId('');
            loadTasks(filterPriority, filterAssignee, filterSearch);
        } catch (err: any) {
            alert(err.response?.data?.detail || 'Erro ao atribuir responsavel');
        }
    };

    const handleDelete = async (taskId: number) => {
        if (!confirm('Deseja excluir esta tarefa?')) return;
        try {
            await api.delete(`/projects/${projectId}/tasks/${taskId}`);
            loadTasks(filterPriority, filterAssignee, filterSearch);
        } catch (err: any) {
            alert(err.response?.data?.detail || 'Erro ao excluir tarefa');
        }
    };

    const changeStatus = async (taskId: number, newStatus: string) => {
        try {
            await api.patch(`/projects/${projectId}/tasks/${taskId}/status`, `"${newStatus}"`, {
                headers: { 'Content-Type': 'application/json' }
            });
            loadTasks(filterPriority, filterAssignee, filterSearch);
        } catch (err: any) {
            alert(err.response?.data?.detail || 'Erro ao mudar status');
        }
    };

    const startEdit = (task: Task) => {
        setEditingTask(task);
        setEditTitle(task.title);
        setEditDescription(task.description || '');
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

    const hasActiveFilters = filterPriority || filterAssignee || filterSearch;

    return (
        <div style={{ padding: 20 }}>
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 20 }}>
                <div>
                    <button onClick={() => navigate('/projects')} style={{ marginRight: 12 }}>Voltar</button>
                    <strong>{projectName || `Projeto #${projectId}`}</strong>
                </div>
                <div style={{ display: 'flex', gap: 8 }}>
                    <button onClick={() => setShowFilters(!showFilters)} style={{ padding: '8px 16px', backgroundColor: hasActiveFilters ? '#fff3e0' : undefined }}>
                        {showFilters ? 'Fechar Filtros' : hasActiveFilters ? 'Filtros (ativos)' : 'Filtros'}
                    </button>
                    <button onClick={loadReport} style={{ padding: '8px 16px' }}>
                        Relatorio
                    </button>
                    <button onClick={() => setShowForm(!showForm)} style={{ padding: '8px 16px' }}>
                        {showForm ? 'Cancelar' : 'Nova Tarefa'}
                    </button>
                </div>
            </div>

            {showFilters && (
                <div style={{ marginBottom: 20, padding: 16, border: '1px solid #ff9800', borderRadius: 8, backgroundColor: '#fff8e1' }}>
                    <h4 style={{ margin: '0 0 8px' }}>Filtros</h4>
                    <div style={{ display: 'flex', gap: 8, flexWrap: 'wrap', alignItems: 'center' }}>
                        <input
                            placeholder="Buscar no titulo ou descricao..."
                            value={filterSearch}
                            onChange={e => setFilterSearch(e.target.value)}
                            style={{ padding: 8, flex: 2, minWidth: 200 }}
                        />
                        <select value={filterPriority} onChange={e => setFilterPriority(e.target.value)} style={{ padding: 8 }}>
                            <option value="">Todas prioridades</option>
                            <option value="LOW">Baixa</option>
                            <option value="MEDIUM">Media</option>
                            <option value="HIGH">Alta</option>
                            <option value="CRITICAL">Critica</option>
                        </select>
                        <select value={filterAssignee} onChange={e => setFilterAssignee(e.target.value)} style={{ padding: 8 }}>
                            <option value="">Todos responsaveis</option>
                            {members.map(m => (
                                <option key={m.id} value={m.id}>{m.name}</option>
                            ))}
                        </select>
                        <button onClick={applyFilters} style={{ padding: '8px 16px' }}>Filtrar</button>
                        {hasActiveFilters && (
                            <button onClick={clearFilters} style={{ padding: '8px 16px', backgroundColor: '#ffebee' }}>Limpar</button>
                        )}
                    </div>
                </div>
            )}

            {showReport && report && (
                <div style={{ marginBottom: 20, padding: 16, border: '2px solid #4caf50', borderRadius: 8, backgroundColor: '#e8f5e9' }}>
                    <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                        <h4 style={{ margin: 0 }}>Relatorio do Projeto</h4>
                        <button onClick={() => setShowReport(false)} style={{ fontSize: 12 }}>Fechar</button>
                    </div>
                    <div style={{ display: 'flex', gap: 40, marginTop: 12 }}>
                        <div>
                            <strong>Por Status:</strong>
                            {Object.entries(report.byStatus).map(([key, val]) => (
                                <div key={key} style={{ fontSize: 14 }}>{statusLabel[key] || key}: {val}</div>
                            ))}
                        </div>
                        <div>
                            <strong>Por Prioridade:</strong>
                            {Object.entries(report.byPriority).map(([key, val]) => (
                                <div key={key} style={{ fontSize: 14 }}>{priorityLabel[key] || key}: {val}</div>
                            ))}
                        </div>
                    </div>
                </div>
            )}

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
                    <div style={{ marginBottom: 8, display: 'flex', gap: 8 }}>
                        <select value={priority} onChange={e => setPriority(e.target.value)} style={{ padding: 8 }}>
                            <option value="LOW">Baixa</option>
                            <option value="MEDIUM">Media</option>
                            <option value="HIGH">Alta</option>
                            <option value="CRITICAL">Critica</option>
                        </select>
                        <select value={assigneeId} onChange={e => setAssigneeId(e.target.value)} style={{ padding: 8, flex: 1 }}>
                            <option value="">Sem responsavel</option>
                            {members.map(m => (
                                <option key={m.id} value={m.id}>{m.name}</option>
                            ))}
                        </select>
                    </div>
                    <button type="submit" style={{ padding: '8px 16px' }}>Criar</button>
                </form>
            )}

            {editingTask && (
                <form onSubmit={handleEdit} style={{ marginBottom: 20, padding: 16, border: '2px solid #1976d2', borderRadius: 8, backgroundColor: '#e3f2fd' }}>
                    <h4 style={{ margin: '0 0 8px' }}>Editando: {editingTask.title}</h4>
                    <div style={{ marginBottom: 8 }}>
                        <input
                            placeholder="Titulo"
                            value={editTitle}
                            onChange={e => setEditTitle(e.target.value)}
                            required
                            style={{ width: '100%', padding: 8 }}
                        />
                    </div>
                    <div style={{ marginBottom: 8 }}>
                        <input
                            placeholder="Descricao"
                            value={editDescription}
                            onChange={e => setEditDescription(e.target.value)}
                            style={{ width: '100%', padding: 8 }}
                        />
                    </div>
                    <button type="submit" style={{ padding: '8px 16px', marginRight: 8 }}>Salvar</button>
                    <button type="button" onClick={() => setEditingTask(null)} style={{ padding: '8px 16px' }}>Cancelar</button>
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
                                            {priorityLabel[task.priority] || task.priority}
                                        </span>
                                    </div>
                                    {task.description && (
                                        <p style={{ margin: '4px 0', fontSize: 13, color: '#666' }}>{task.description}</p>
                                    )}
                                    <div style={{ fontSize: 12, color: '#999', marginBottom: 4 }}>
                                        {task.assigneeName
                                            ? <span>Responsavel: {task.assigneeName}</span>
                                            : <span style={{ fontStyle: 'italic' }}>Sem responsavel</span>
                                        }
                                    </div>

                                    {assigningTaskId === task.id && (
                                        <div style={{ display: 'flex', gap: 4, marginBottom: 4 }}>
                                            <select
                                                value={newAssigneeId}
                                                onChange={e => setNewAssigneeId(e.target.value)}
                                                style={{ padding: 4, fontSize: 11, flex: 1 }}
                                            >
                                                <option value="">Sem responsavel</option>
                                                {members.map(m => (
                                                    <option key={m.id} value={m.id}>{m.name}</option>
                                                ))}
                                            </select>
                                            <button onClick={() => handleAssign(task.id)} style={{ fontSize: 11, padding: '4px 8px' }}>OK</button>
                                            <button onClick={() => setAssigningTaskId(null)} style={{ fontSize: 11, padding: '4px 8px' }}>X</button>
                                        </div>
                                    )}

                                    <div style={{ marginTop: 4, display: 'flex', gap: 4, flexWrap: 'wrap' }}>
                                        {getNextStatuses(task.status).map(action => (
                                            <button
                                                key={action.status}
                                                onClick={() => changeStatus(task.id, action.status)}
                                                style={{ fontSize: 11, padding: '4px 8px' }}
                                            >
                                                {action.label}
                                            </button>
                                        ))}
                                        <button
                                            onClick={() => {
                                                setAssigningTaskId(assigningTaskId === task.id ? null : task.id);
                                                setNewAssigneeId('');
                                            }}
                                            style={{ fontSize: 11, padding: '4px 8px', backgroundColor: '#f3e5f5' }}
                                        >
                                            Atribuir
                                        </button>
                                        <button
                                            onClick={() => startEdit(task)}
                                            style={{ fontSize: 11, padding: '4px 8px', backgroundColor: '#e3f2fd' }}
                                        >
                                            Editar
                                        </button>
                                        <button
                                            onClick={() => handleDelete(task.id)}
                                            style={{ fontSize: 11, padding: '4px 8px', backgroundColor: '#ffebee', color: '#c62828' }}
                                        >
                                            Excluir
                                        </button>
                                    </div>
                                </div>
                            ))}
                    </div>
                ))}
            </div>
        </div>
    );
}
