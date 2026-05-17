import { BrowserRouter, Routes, Route } from 'react-router-dom';
import { AuthProvider } from './context/AuthContext';
import { ToastContainer } from 'react-toastify';

// Layout components
import Navbar from './components/Navbar';
import ProtectedRoute from './components/ProtectedRoute';
import AdminRoute from './components/AdminRoute';

// Page components — all 9 pages
import LandingPage from './pages/LandingPage';
import LoginPage from './pages/LoginPage';
import RegisterPage from './pages/RegisterPage';
import DashboardPage from './pages/DashboardPage';
import CreateLinkPage from './pages/CreateLinkPage';
import LinkDetailPage from './pages/LinkDetailPage';
import BulkUploadPage from './pages/BulkUploadPage';
import AdminLinksPage from './pages/AdminLinksPage';
import AdminUsersPage from './pages/AdminUsersPage';

/**
 * App — the root component with complete routing.
 *
 * Route structure:
 *   / (public)
 *   ├── /              → LandingPage
 *   ├── /login         → LoginPage
 *   └── /register      → RegisterPage
 *
 *   / (protected — requires login)
 *   ├── /dashboard              → DashboardPage
 *   ├── /dashboard/create       → CreateLinkPage
 *   ├── /dashboard/links/:key   → LinkDetailPage
 *   └── /dashboard/bulk         → BulkUploadPage
 *
 *   / (admin — requires ADMIN role)
 *   ├── /admin/links   → AdminLinksPage
 *   └── /admin/users   → AdminUsersPage
 */
export default function App() {
  return (
    <AuthProvider>
      <BrowserRouter>
        <Navbar />
        <main className="container mx-auto px-4 py-8">
          <Routes>
            {/* ── Public Routes ──────────────────────────────── */}
            <Route path="/" element={<LandingPage />} />
            <Route path="/login" element={<LoginPage />} />
            <Route path="/register" element={<RegisterPage />} />

            {/* ── Protected Routes (requires login) ──────────── */}
            <Route element={<ProtectedRoute />}>
              <Route path="/dashboard" element={<DashboardPage />} />
              <Route path="/dashboard/create" element={<CreateLinkPage />} />
              <Route path="/dashboard/links/:shortKey" element={<LinkDetailPage />} />
              <Route path="/dashboard/bulk" element={<BulkUploadPage />} />
            </Route>

            {/* ── Admin Routes (requires ADMIN role) ─────────── */}
            <Route element={<AdminRoute />}>
              <Route path="/admin/links" element={<AdminLinksPage />} />
              <Route path="/admin/users" element={<AdminUsersPage />} />
            </Route>
          </Routes>
        </main>
        <ToastContainer position="bottom-right" autoClose={3000} />
      </BrowserRouter>
    </AuthProvider>
  );
}