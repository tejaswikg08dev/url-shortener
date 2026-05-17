import{useState} from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { toast } from 'react-toastify';
import { useAuth } from '../context/AuthContext';

export default function LoginPage(){
    const[email, setEmail] = useState('');
    const[password, setPassword] = useState('');
    const[loading, setLoading] = useState(false);

    const{login} = useAuth();
    const navigate = useNavigate();

    const handleSubmit = async (e) => {

        e.preventDefault();

        setLoading(true);
        try{
            await login(email, password);
            toast.success('Welcome back!');
            navigate('/dashboard');
        } catch(err){
            toast.error(err.response?.data?.message || 'Login failed');
        } finally{
            setLoading(false);
        }
    };

    return (
        <div className="max-w-md mx-auto mt-20">
      <h1 className="text-2xl font-bold mb-6 text-center">Login</h1>

      {/* onSubmit fires when the form is submitted (button click or Enter key) */}
      <form onSubmit={handleSubmit} className="space-y-4">
        <div>
          <label className="block text-sm font-medium text-gray-700 mb-1">Email</label>
          <input
            type="email"           // Browser validates email format
            value={email}          // Controlled: value comes from state
            onChange={(e) => setEmail(e.target.value)}  // Update state on every keystroke
            className="w-full px-4 py-2 border rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500"
            required               // HTML5 validation: browser won't submit if empty
          />
        </div>

        <div>
          <label className="block text-sm font-medium text-gray-700 mb-1">Password</label>
          <input
            type="password"        // Hides the characters
            value={password}
            onChange={(e) => setPassword(e.target.value)}
            className="w-full px-4 py-2 border rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500"
            required
          />
        </div>

        <button
          type="submit"            // Makes this button trigger the form's onSubmit
          disabled={loading}       // Prevents double-click while API call is in progress
          className="w-full bg-blue-600 text-white py-2 rounded-lg hover:bg-blue-700 disabled:opacity-50"
        >
          {/* Ternary: show different text based on loading state */}
          {loading ? 'Logging in...' : 'Login'}
        </button>
      </form>

      {/* Link to register page */}
      <p className="mt-4 text-center text-gray-600">
        Don't have an account?{' '}
        {/* {' '} adds a space in JSX (whitespace between elements is trimmed) */}
        <Link to="/register" className="text-blue-600 hover:underline">Sign up</Link>
      </p>
    </div>
    );
}