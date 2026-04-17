import { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import api from '../api';

interface Member {
    id: number;
    name: string;
}

interface Project {
    id: number;
    name: string;
    description: string;
    ownerName: string;
    members: Member[];
}

export default function ProjectsPage() {
    const [projects, setProjects] = useState<Project[]>([]);
    const [name, setName] = useState('');
    const [description, setDescription] = useState('');
    const [addMemberProjectId, setAddMemberProjectId] = useState<number | null>(null);
    const [memberId, setMemberId] = useState('');
    const [memberError, setMemberError] = useState('');
    const [editingProject, setEditingProject] = useState<Project | null>(null);
    const [editName, setEditName] = useState('');
    const [editDescription, setEditDescription] = useState('');
    const navigate = useNavigate();
    const userName = localStorage.getItem('userName');

    useEffect(() => {
        loadProjects();
    }, []);

    const loadProjects = async () => {
        try {
            const res = await api.get('/projects');
            setProjects(res.data);
        } catch {
            localStorage.clear();
            navigate('/');
        }
    };

    const handleCreate = async (e: React.FormEvent) => {
        e.preventDefault();
        await api.post('/projects', { name, description });
        setName('');
        setDescription('');
        loadProjects();
    };

    const handleAddMember = async (e: React.FormEvent) => {
        e.preventDefault();
        setMemberError('');
        try {
            await api.post(`/projects/${addMemberProjectId}/members/${memberId}`);
            setMemberId('');
            setAddMemberProjectId(null);
            loadProjects();
        } catch (err: any) {
            setMemberError(err.response?.data?.detail || 'Erro ao adicionar membro');
        }
    };

    const handleRemoveMember = async (projectId: number, userId: number) => {
        if (!confirm('Deseja remover este membro?')) return;
        try {
            await api.delete(`/projects/${projectId}/members/${userId}`);
            loadProjects();
        } catch (err: any) {
            alert(err.response?.data?.detail || 'Erro ao remover membro');
        }
    };

    const handleEditProject = async (e: React.FormEvent) => {
        e.preventDefault();
        if (!editingProject) return;
        try {
            await api.put(`/projects/${editingProject.id}`, {
                name: editName,
                description: editDescription,
            });
            setEditingProject(null);
            loadProjects();
        } catch (err: any) {
            alert(err.response?.data?.detail || 'Erro ao atualizar projeto');
        }
    };

    const startEditProject = (p: Project) => {
        setEditingProject(p);
        setEditName(p.name);
        setEditDescription(p.description || '');
    };

    const logout = () => {
        localStorage.clear();
        navigate('/');
    };

    return (
        <div style={{ maxWidth: 800, margin: '40px auto', padding: 20 }}>
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                <h1>Projetos</h1>
                <div>
                    <span style={{ marginRight: 12 }}>{userName}</span>
                    <button onClick={logout}>Sair</button>
                </div>
            </div>

            <form onSubmit={handleCreate} style={{ marginBottom: 30, padding: 16, border: '1px solid #ccc', borderRadius: 8 }}>
                <h3>Novo Projeto</h3>
                <div style={{ marginBottom: 8 }}>
                    <input
                        placeholder="Nome do projeto"
                        value={name}
                        onChange={e => setName(e.target.value)}
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
                <button type="submit" style={{ padding: '8px 16px' }}>Criar</button>
            </form>

            {editingProject && (
                <form onSubmit={handleEditProject} style={{ marginBottom: 20, padding: 16, border: '2px solid #1976d2', borderRadius: 8, backgroundColor: '#e3f2fd' }}>
                    <h4 style={{ margin: '0 0 8px' }}>Editando: {editingProject.name}</h4>
                    <div style={{ marginBottom: 8 }}>
                        <input
                            placeholder="Nome do projeto"
                            value={editName}
                            onChange={e => setEditName(e.target.value)}
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
                    <button type="button" onClick={() => setEditingProject(null)} style={{ padding: '8px 16px' }}>Cancelar</button>
                </form>
            )}

            <div>
                {projects.map(p => (
                    <div
                        key={p.id}
                        style={{
                            padding: 16,
                            marginBottom: 12,
                            border: '1px solid #ccc',
                            borderRadius: 8,
                        }}
                    >
                        <div
                            onClick={() => navigate(`/projects/${p.id}/board`)}
                            style={{ cursor: 'pointer' }}
                        >
                            <h3 style={{ margin: 0 }}>{p.name}</h3>
                            <p style={{ margin: '4px 0', color: '#666' }}>{p.description}</p>
                        </div>
                        <div style={{ margin: '8px 0' }}>
                            <small>Dono: {p.ownerName}</small>
                            <div style={{ marginTop: 4 }}>
                                <small>Membros: </small>
                                {p.members.map(m => (
                                    <span key={m.id} style={{
                                        display: 'inline-flex',
                                        alignItems: 'center',
                                        backgroundColor: '#e3f2fd',
                                        borderRadius: 12,
                                        padding: '2px 8px',
                                        margin: '2px 4px',
                                        fontSize: 12,
                                    }}>
                                        {m.name}
                                        {m.name !== p.ownerName && (
                                            <button
                                                onClick={(e) => {
                                                    e.stopPropagation();
                                                    handleRemoveMember(p.id, m.id);
                                                }}
                                                style={{
                                                    marginLeft: 4, border: 'none', background: 'none',
                                                    cursor: 'pointer', color: '#c62828', fontSize: 12, padding: 0,
                                                }}
                                            >
                                                x
                                            </button>
                                        )}
                                    </span>
                                ))}
                            </div>
                        </div>
                        <div style={{ display: 'flex', gap: 8 }}>
                            <button
                                onClick={(e) => {
                                    e.stopPropagation();
                                    startEditProject(p);
                                }}
                                style={{ fontSize: 12, padding: '4px 8px', backgroundColor: '#e3f2fd' }}
                            >
                                Editar
                            </button>
                            <button
                                onClick={(e) => {
                                    e.stopPropagation();
                                    setAddMemberProjectId(addMemberProjectId === p.id ? null : p.id);
                                    setMemberError('');
                                    setMemberId('');
                                }}
                                style={{ fontSize: 12, padding: '4px 8px' }}
                            >
                                {addMemberProjectId === p.id ? 'Cancelar' : 'Adicionar Membro'}
                            </button>
                        </div>

                        {addMemberProjectId === p.id && (
                            <form onSubmit={handleAddMember} style={{ marginTop: 8, display: 'flex', gap: 8, alignItems: 'center' }}>
                                <input
                                    placeholder="ID do usuario"
                                    value={memberId}
                                    onChange={e => setMemberId(e.target.value)}
                                    required
                                    style={{ padding: 6, flex: 1 }}
                                />
                                <button type="submit" style={{ padding: '6px 12px', fontSize: 12 }}>Adicionar</button>
                                {memberError && <span style={{ color: 'red', fontSize: 12 }}>{memberError}</span>}
                            </form>
                        )}
                    </div>
                ))}
                {projects.length === 0 && <p>Nenhum projeto encontrado. Crie um acima.</p>}
            </div>
        </div>
    );
}
