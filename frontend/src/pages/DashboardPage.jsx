import { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import { toast } from 'react-toastify';
import { HiPlus, HiTrash, HiSearch, HiClipboardCopy } from 'react-icons/hi';
import urlService from '../services/urlService';
import Pagination from '../components/Pagination';
import { formatDate, formatNumber, truncateUrl } from '../utils/formatters';

export default function DashboardPage() {
  const [urls, setUrls] = useState([]);
  const [loading, setLoading] = useState(true);
  const [search, setSearch] = useState('');
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);

  const fetchUrls = async () => {
    setLoading(true);
    try  {
        const {data} = await urlService.list(page, 10, search);
        setUrls(data.content);
        setTotalPages(data.totalPages);
    } catch (err) {
        toast.error('Failed to load URLs');
    } finally {
        setLoading(false);
    }
  };

    useEffect(() => {
        fetchUrls();
    }, [page, search]);

    const handleDelete = async (shortKey) => {
        if (!window.confirm('Are you sure you want to delete this URL?')) return;
        try {
            await urlService.delete(shortKey);
            toast.success('URL deleted');
            fetchUrls();
        } catch (err) {
            toast.error('Failed to delete URL');
        }
    };

    const copyUrl = (shortKey) => {
        navigator.clipboard.writeText(shortKey);
        toast.success('Copied!');
    };

    return (
        <div>
      {/* ── Header with title and action buttons ──────────────── */}
      <div className="flex items-center justify-between mb-6">
        <h1 className="text-2xl font-bold">My Links</h1>
        <div className="flex gap-2">
          <Link
            to="/dashboard/bulk"
            className="border border-blue-600 text-blue-600 px-4 py-2 rounded-lg hover:bg-blue-50"
          >
            Bulk Upload
          </Link>
          <Link
            to="/dashboard/create"
            className="bg-blue-600 text-white px-4 py-2 rounded-lg hover:bg-blue-700 flex items-center gap-1"
          >
            <HiPlus /> New Link
          </Link>
        </div>
      </div>

      {/* ── Search bar ────────────────────────────────────────── */}
      <div className="mb-4 relative">
        {/* Position the search icon inside the input using absolute positioning */}
        <HiSearch className="absolute left-3 top-3 text-gray-400" />
        <input
          type="text"
          value={search}
          onChange={(e) => {
            setSearch(e.target.value);
            setPage(0);  // Reset to page 0 when search changes
            // Why? If you're on page 5 and search for something,
            // the results might only have 1 page. Page 5 would be empty.
          }}
          placeholder="Search URLs..."
          className="w-full pl-10 pr-4 py-2 border rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500"
        />
      </div>

      {/* ── Conditional rendering: loading → empty → table ─────── */}
      {loading ? (
        <div className="text-center py-8 text-gray-500">Loading...</div>
      ) : urls.length === 0 ? (
        // Empty state — shown when user has no URLs (or search has no results)
        <div className="text-center py-12 text-gray-500">
          <p className="text-lg mb-2">No links yet</p>
          <Link to="/dashboard/create" className="text-blue-600 hover:underline">
            Create your first short link
          </Link>
        </div>
      ) : (
        // URL table — shown when there are URLs to display
        <div className="bg-white rounded-lg shadow overflow-hidden">
          <table className="w-full">
            <thead className="bg-gray-50">
              <tr>
                <th className="px-4 py-3 text-left text-sm font-medium text-gray-600">Short URL</th>
                <th className="px-4 py-3 text-left text-sm font-medium text-gray-600">Original URL</th>
                <th className="px-4 py-3 text-center text-sm font-medium text-gray-600">Clicks</th>
                <th className="px-4 py-3 text-left text-sm font-medium text-gray-600">Created</th>
                <th className="px-4 py-3 text-center text-sm font-medium text-gray-600">Actions</th>
              </tr>
            </thead>
            <tbody className="divide-y">
              {/* .map() renders one <tr> for each URL in the array.
                  'key' is required — React uses it to track which items changed.
                  Use a unique identifier (shortKey), NOT the array index. */}
              {urls.map((url) => (
                <tr key={url.shortKey} className="hover:bg-gray-50">
                  <td className="px-4 py-3">
                    {/* Link to the detail/analytics page for this URL */}
                    <Link
                      to={`/dashboard/links/${url.shortKey}`}
                      className="text-blue-600 hover:underline font-medium"
                    >
                      {url.shortUrl}
                    </Link>
                  </td>
                  <td className="px-4 py-3 text-sm text-gray-600">
                    {truncateUrl(url.longUrl, 60)}
                  </td>
                  <td className="px-4 py-3 text-center font-medium">
                    {formatNumber(url.clickCount)}
                  </td>
                  <td className="px-4 py-3 text-sm text-gray-500">
                    {formatDate(url.createdAt)}
                  </td>
                  <td className="px-4 py-3">
                    <div className="flex items-center justify-center gap-2">
                      <button
                        onClick={() => copyUrl(url.shortUrl)}
                        className="text-gray-400 hover:text-blue-600"
                        title="Copy"
                      >
                        <HiClipboardCopy />
                      </button>
                      <button
                        onClick={() => handleDelete(url.shortKey)}
                        className="text-gray-400 hover:text-red-600"
                        title="Delete"
                      >
                        <HiTrash />
                      </button>
                    </div>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}

      {/* ── Pagination ────────────────────────────────────────── */}
      {/* When user clicks Next/Previous, setPage updates → useEffect re-runs → new data */}
      <Pagination page={page} totalPages={totalPages} onPageChange={setPage} />
    </div>
    );
}