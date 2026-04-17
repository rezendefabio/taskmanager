import { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import api from '../api';

interface Project {
    id: number;
    name: string;
    description: string;
    ownerName: string;
    members: string[];
}

export default function ProjectsPage() {
    const [projects, setProjects] = useState<Project[]>([]);
    const [name, setName] = useState('');
    const [description, setDescription] = useState('');
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

            <div>
                {projects.map(p => (
                    <div
                        key={p.id}
                        onClick={() => navigate(`/projects/${p.id}/board`)}
                        style={{
                            padding: 16,
                            marginBottom: 12,
                            border: '1px solid #ccc',
                            borderRadius: 8,
                            cursor: 'pointer'
                        }}
                    >
                        <h3 style={{ margin: 0 }}>{p.name}</h3>
                        <p style={{ margin: '4px 0', color: '#666' }}>{p.description}</p>
                        <small>Dono: {p.ownerName} | Membros: {p.members.join(', ')}</small>
                    </div>
                ))}
                {projects.length === 0 && <p>Nenhum projeto encontrado. Crie um acima.</p>}
            </div>
        </div>
    );
}
