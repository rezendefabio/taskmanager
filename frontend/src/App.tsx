import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import LoginPage from './pages/LoginPage';
import ProjectsPage from './pages/ProjectsPage';
import BoardPage from './pages/BoardPage';

export default function App() {
    return (
        <BrowserRouter>
            <Routes>
                <Route path="/" element={<LoginPage />} />
                <Route path="/projects" element={<ProjectsPage />} />
                <Route path="/projects/:projectId/board" element={<BoardPage />} />
                <Route path="*" element={<Navigate to="/" />} />
            </Routes>
        </BrowserRouter>
    );
}
