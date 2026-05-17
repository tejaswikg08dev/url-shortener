import {useState} from 'react';
import { useNavigate } from 'react-router-dom';
import { toast } from 'react-toastify';
import urlService from '../services/urlService';
import {HiClipboardCopy} from 'react-icons/hi';

export default function CreateLinkPage() {
    const [longUrl, setLongUrl] = useState('');
    const [customAlias, setCustomAlias] = useState('');
    const [expiresAt, setExpiresAt] = useState('');
    const [tags, setTags] = useState('');

    const [result, setResult] = useState(null);
    const [loading, setLoading] = useState(false);
    const navigate = useNavigate();

    const handleSubmit = async (e) => {
        e.preventDefault();
        setLoading(true);
        try {
            const payload = {
                longUrl,
                customAlias: customAlias || null,
                expiresAt: expiresAt ? new Date(expiresAt).toDateString() : null,
                tags: tags ? tags.split(',').map((t) => tag.trim()).filter(Boolean) : []
            };
            const {data} = await urlService.create(payload);
            setResult(data);
            toast.success('Link created successfully!');
        } catch (err) {
            toast.error(err.response?.data?.message || 'Failed to create link');
        } finally {
            setLoading(false);
        }  
    };

    const copyUrl = () => {
        navigator.clipboard.writeText(result.shortKey);
        toast.success('Copied to clipboard!');
    };

    return (
        <div className="max-w-lg mx-auto">
      <h1 className="text-2xl font-bold mb-6">Create New Link</h1>

      {/* Toggle between result view and form view based on 'result' state */}
      {result ? (
        // ── Result View ──────────────────────────────────────────
        <div className="bg-green-50 border border-green-200 rounded-lg p-6 text-center">
          <p className="text-sm text-gray-600 mb-2">Your short link is ready:</p>
          <div className="flex items-center justify-center gap-2 mb-4">
            <a
              href={result.shortUrl}
              target="_blank"
              rel="noopener noreferrer"
              className="text-xl text-blue-600 font-bold hover:underline"
            >
              {result.shortUrl}
            </a>
            <button onClick={copyUrl} className="text-gray-500 hover:text-blue-600">
              <HiClipboardCopy className="text-xl" />
            </button>
          </div>
          <div className="flex gap-2 justify-center">
            <button
              onClick={() => { setResult(null); setLongUrl(''); setCustomAlias(''); setExpiresAt(''); setTags(''); }}
              className="border px-4 py-2 rounded-lg hover:bg-gray-100"
            >
              Create Another
            </button>
            <button
              onClick={() => navigate(`/dashboard/links/${result.shortKey}`)}
              className="bg-blue-600 text-white px-4 py-2 rounded-lg hover:bg-blue-700"
            >
              View Analytics
            </button>
          </div>
        </div>
      ) : (
        // ── Form View ────────────────────────────────────────────
        <form onSubmit={handleSubmit} className="space-y-4">
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">
              Long URL <span className="text-red-500">*</span>
            </label>
            <input
              type="url"
              value={longUrl}
              onChange={(e) => setLongUrl(e.target.value)}
              placeholder="https://example.com/very/long/url"
              className="w-full px-4 py-2 border rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500"
              required
            />
          </div>

          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">
              Custom Alias (optional)
            </label>
            <input
              type="text"
              value={customAlias}
              onChange={(e) => setCustomAlias(e.target.value)}
              placeholder="my-custom-link"
              className="w-full px-4 py-2 border rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500"
              minLength={3}
              maxLength={20}
              // pattern validates the input format (HTML5 validation)
              // Only allows: letters, numbers, hyphens, underscores
              pattern="^[a-zA-Z0-9\-_]+$"
            />
            <p className="text-xs text-gray-500 mt-1">Letters, numbers, hyphens, underscores. 3-20 chars.</p>
          </div>

          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">
              Expiration Date (optional)
            </label>
            {/* datetime-local gives a date+time picker in the browser */}
            <input
              type="datetime-local"
              value={expiresAt}
              onChange={(e) => setExpiresAt(e.target.value)}
              className="w-full px-4 py-2 border rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500"
            />
          </div>

          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">
              Tags (optional, comma-separated)
            </label>
            <input
              type="text"
              value={tags}
              onChange={(e) => setTags(e.target.value)}
              placeholder="marketing, social, campaign"
              className="w-full px-4 py-2 border rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500"
            />
          </div>

          <button
            type="submit"
            disabled={loading}
            className="w-full bg-blue-600 text-white py-2 rounded-lg hover:bg-blue-700 disabled:opacity-50"
          >
            {loading ? 'Creating...' : 'Create Short Link'}
          </button>
        </form>
      )}
    </div>
    );
}