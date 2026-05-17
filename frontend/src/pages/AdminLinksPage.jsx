import { useState, useEffect } from 'react';
import { toast } from 'react-toastify';
import { HiTrash } from 'react-icons/hi';
import adminService from '../services/adminService';
import Pagination from '../components/Pagination';
import { formatDate, truncateUrl, formatNumber } from '../utils/formatters';

export default function AdminLinksPage() {
    const [urls, setUrls] = useState([]);
    const [loading, setLoading] = useState(true);
    const [page, setPage] = useState(0);
    const [totalPages, setTotalPages] = useState(0);

    const fetchUrls = async () => {
        setLoading(true);
        try {
            const { data } = await adminService.getAllUrls(page, 20);
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
    }, [page]);

    const handleDelete = async (shortKey) => {
        if (!window.confirm('Are you sure you want to delete this URL?')) return;
        try {
            await adminService.deleteUrl(shortKey);
            toast.success('URL deleted');
            fetchUrls();
        } catch (err) {
            toast.error('Failed to delete URL');
        }
    };

    return (
        <div>
      <h1 className="text-2xl font-bold mb-6">Admin — All Links</h1>

      {loading ? (
        <p className="text-center py-8">Loading...</p>
      ) : (
        <div className="bg-white rounded-lg shadow overflow-hidden">
          <table className="w-full">
            <thead className="bg-gray-50">
              <tr>
                <th className="px-4 py-3 text-left text-sm font-medium text-gray-600">Short Key</th>
                <th className="px-4 py-3 text-left text-sm font-medium text-gray-600">Original URL</th>
                <th className="px-4 py-3 text-center text-sm font-medium text-gray-600">Clicks</th>
                <th className="px-4 py-3 text-left text-sm font-medium text-gray-600">Created</th>
                <th className="px-4 py-3 text-center text-sm font-medium text-gray-600">Actions</th>
              </tr>
            </thead>
            <tbody className="divide-y">
              {urls.map((url) => (
                <tr key={url.shortKey} className="hover:bg-gray-50">
                  <td className="px-4 py-3 font-medium text-blue-600">{url.shortKey}</td>
                  <td className="px-4 py-3 text-sm text-gray-600">{truncateUrl(url.longUrl, 60)}</td>
                  <td className="px-4 py-3 text-center">{formatNumber(url.clickCount)}</td>
                  <td className="px-4 py-3 text-sm text-gray-500">{formatDate(url.createdAt)}</td>
                  <td className="px-4 py-3 text-center">
                    <button
                      onClick={() => handleDelete(url.shortKey)}
                      className="text-red-500 hover:text-red-700"
                      title="Delete"
                    >
                      <HiTrash />
                    </button>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}

      <Pagination page={page} totalPages={totalPages} onPageChange={setPage} />
    </div>
    );
}