import { Link, useNavigate } from "react-router-dom";
import { useAuth } from "../context/AuthContext";
import { HiLink } from "react-icons/hi";


export default function Navbar(){
    const{user, logout, isAdmin} = useAuth();
    const navigate = useNavigate();

    const handleLogout = () => {
        logout();
        navigate('/');
    };

    return (
        <nav className="bg-white shadow-sm border-b">
      <div className="container mx-auto px-4 py-3 flex items-center justify-between">
        {/* Logo / Home link */}
        <Link to="/" className="flex items-center gap-2 text-xl font-bold text-blue-600">
          <HiLink className="text-2xl" />
          URL Shortener
        </Link>

        <div className="flex items-center gap-4">
          {/* Conditional rendering based on auth state */}
          {user ? (
            // ── Logged In State ──────────────────────────────
            // <> is a "Fragment" — groups elements without adding a DOM node.
            // React requires a single parent element, Fragment is invisible.
            <>
              <Link to="/dashboard" className="text-gray-600 hover:text-blue-600">
                Dashboard
              </Link>

              {/* Only show admin links if user has ADMIN role */}
              {isAdmin() && (
                <>
                  <Link to="/admin/links" className="text-gray-600 hover:text-blue-600">
                    Admin Links
                  </Link>
                  <Link to="/admin/users" className="text-gray-600 hover:text-blue-600">
                    Admin Users
                  </Link>
                </>
              )}

              {/* Show user's name */}
              <span className="text-sm text-gray-500">{user.name}</span>

              {/* Logout button */}
              <button
                onClick={handleLogout}
                className="text-sm text-red-500 hover:text-red-700"
              >
                Logout
              </button>
            </>
          ) : (
            // ── Logged Out State ─────────────────────────────
            <>
              <Link to="/login" className="text-gray-600 hover:text-blue-600">
                Login
              </Link>
              <Link
                to="/register"
                className="bg-blue-600 text-white px-4 py-2 rounded-lg hover:bg-blue-700"
              >
                Sign Up
              </Link>
            </>
          )}
        </div>
      </div>
    </nav>
    );
}