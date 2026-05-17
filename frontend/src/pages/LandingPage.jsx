import{useState} from 'react';
import { Link } from 'react-router-dom';
import { toast } from 'react-toastify';
import { HiClipboardCopy, HiLink } from 'react-icons/hi';
import apiClient from '../services/apiClient';
import { useAuth } from '../context/AuthContext';

export default function LandingPage(){
    const{user} = useAuth();
    const[longUrl, setLongUrl] = useState('');
    const[shortUrl, setShortUrl] = useState('');
    const[loading, setLoading] = useState(false);

    const handleSubmit = async (e) => {

        e.preventDefault();
        if(!longUrl.trim()) return;

        if(!user){
            toast.info('Please login to shorten URLs');
            return;
        }
        setLoading(true);
        try{
            const {data}= await apiClient.post('/urls', { longUrl});
            setShortUrl(data.shortUrl);
            toast.success('URL shortened!');
        } catch(err){
            toast.error(err.response?.data?.message || 'Failed to shorten URL');
        } finally{
            setLoading(false);
        }
    };

    const copyToClipboard = () => {
        navigator.clipboard.writeText(shortUrl);
        toast.success('Copied to clipboard!');
    };

    return (
        <div className="max-w-2xl mx-auto mt-20 text-center">
      <HiLink className="text-6xl text-blue-600 mx-auto mb-4" />
      <h1 className="text-4xl font-bold mb-4">Shorten Your URLs</h1>
      <p className="text-gray-600 mb-8">
        Create short, memorable links with analytics tracking.
      </p>

      {/* URL shortener form */}
      <form onSubmit={handleSubmit} className="flex gap-2 mb-6">
        <input
          type="url"
          value={longUrl}
          onChange={(e) => setLongUrl(e.target.value)}
          placeholder="Paste your long URL here..."
          className="flex-1 px-4 py-3 border rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500"
          required
        />
        <button
          type="submit"
          disabled={loading}
          className="bg-blue-600 text-white px-6 py-3 rounded-lg hover:bg-blue-700 disabled:opacity-50"
        >
          {loading ? 'Shortening...' : 'Shorten'}
        </button>
      </form>

      {/* Result card — only shows when shortUrl is not empty */}
      {shortUrl && (
        <div className="bg-green-50 border border-green-200 rounded-lg p-4 flex items-center justify-between">
          <a
            href={shortUrl}
            target="_blank"           // Open in new tab
            rel="noopener noreferrer" // Security: prevents new page from accessing window.opener
            className="text-blue-600 font-medium hover:underline"
          >
            {shortUrl}
          </a>
          <button
            onClick={copyToClipboard}
            className="flex items-center gap-1 text-gray-600 hover:text-blue-600"
          >
            <HiClipboardCopy /> Copy
          </button>
        </div>
      )}

      {/* Sign-up prompt for non-authenticated visitors */}
      {!user && (
        <p className="mt-8 text-gray-500">
          <Link to="/register" className="text-blue-600 hover:underline">
            Sign up
          </Link>{' '}
          for free to track clicks, manage links, and view analytics.
        </p>
      )}
    </div>
    );
}