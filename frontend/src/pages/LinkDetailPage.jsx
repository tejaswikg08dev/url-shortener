import { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { toast } from 'react-toastify';
import { HiClipboardCopy, HiTrash, HiArrowLeft } from 'react-icons/hi';

import { LineChart, Line, XAxis, YAxis, Tooltip, ResponsiveContainer, CartesianGrid,
    PieChart, Pie, Cell, 
    BarChart, Bar
 } from 'recharts';

import urlService from '../services/urlService';
import { formatDate, formatNumber } from '../utils/formatters';
import analyticsService from '../services/analyticsService';

const COLORS = ['#3B82F6', '#10B981', '#F59E0B', '#EF4444', '#8B5CF6', '#EC4899'];

export default function LinkDetailPage() {
    const { shortKey } = useParams();

    const navigate = useNavigate();

    const [url, setUrl] = useState(null);
    const [stats, setStats] = useState(null);
    const [days, setDays] = useState(30);
    const [loading, setLoading] = useState(true);

    useEffect(() => {
        const fetchData = async () => {
            setLoading(true);
            try {
                const [urlRes, statsRes] = await Promise.all([
                    urlService.get(shortKey),
                    analyticsService.getStats(shortKey, days)
                ]);
                setUrl(urlRes.data);
                setStats(statsRes.data);
            } catch (err) {
                toast.error('Failed to load link details');
                navigate('/dashboard');
            } finally {
                setLoading(false);
            }
        };
        fetchData();
    }, [shortKey, days]);

    const handleDelete = async () => {
        if (!window.confirm('Are you sure you want to delete this link?')) return; 
        try {
            await urlService.delete(shortKey);
            toast.success('URL deleted');
            navigate('/dashboard');
        } catch (err) {
            toast.error('Failed to delete URL');
        }
    };

    const handleExport = async () => {
        try {
            const { data } = await analyticsService.exportCsv(shortKey);
            const blob = new Blob([data], { type: 'text/csv' });
            const link = document.createElement('a');
            link.href = URL.createObjectURL(blob);
            link.download = `analytics-${shortKey}.csv`;
            link.click();
        } catch (err) {
            toast.error('Failed to export analytics');
        }
    };

    if(loading) return <div className="text-center py-8">Loading...</div>;
    if(!url) return null;

    return (
        <div>
      {/* Back button */}
      <button
        onClick={() => navigate('/dashboard')}
        className="flex items-center gap-1 text-gray-600 hover:text-blue-600 mb-4"
      >
        <HiArrowLeft /> Back to Dashboard
      </button>

      {/* ── URL Info Card ─────────────────────────────────────── */}
      <div className="bg-white rounded-lg shadow p-6 mb-6">
        <div className="flex items-center justify-between mb-4">
          <div>
            <a href={url.shortUrl} target="_blank" rel="noopener noreferrer"
               className="text-xl text-blue-600 font-bold hover:underline">
              {url.shortUrl}
            </a>
            {/* break-all prevents long URLs from overflowing the container */}
            <p className="text-sm text-gray-500 mt-1 break-all">{url.longUrl}</p>
          </div>
          <div className="flex gap-2">
            <button
              onClick={() => { navigator.clipboard.writeText(url.shortUrl); toast.success('Copied!'); }}
              className="p-2 border rounded hover:bg-gray-100"
            >
              <HiClipboardCopy />
            </button>
            <button onClick={handleDelete} className="p-2 border rounded text-red-500 hover:bg-red-50">
              <HiTrash />
            </button>
          </div>
        </div>
        <div className="flex gap-6 text-sm text-gray-500">
          <span>Created: {formatDate(url.createdAt)}</span>
          {url.expiresAt && <span>Expires: {formatDate(url.expiresAt)}</span>}
          {url.tags?.length > 0 && <span>Tags: {url.tags.join(', ')}</span>}
        </div>
      </div>

      {/* ── Stats Summary Cards ───────────────────────────────── */}
      <div className="grid grid-cols-3 gap-4 mb-6">
        <div className="bg-white rounded-lg shadow p-4 text-center">
          <p className="text-3xl font-bold text-blue-600">{formatNumber(stats?.totalClicks || 0)}</p>
          <p className="text-sm text-gray-500">Total Clicks</p>
        </div>
        <div className="bg-white rounded-lg shadow p-4 text-center">
          <p className="text-3xl font-bold text-green-600">{formatNumber(stats?.uniqueVisitors || 0)}</p>
          <p className="text-sm text-gray-500">Unique Visitors</p>
        </div>
        <div className="bg-white rounded-lg shadow p-4 text-center">
          {/* Time range selector — clicking a button changes 'days' state,
              which triggers the useEffect to re-fetch with new range */}
          <div className="flex gap-2 justify-center">
            {[7, 30, 90].map((d) => (
              <button
                key={d}
                onClick={() => setDays(d)}
                className={`px-3 py-1 rounded text-sm ${
                  days === d ? 'bg-blue-600 text-white' : 'border hover:bg-gray-100'
                }`}
              >
                {d}d
              </button>
            ))}
          </div>
          <p className="text-sm text-gray-500 mt-2">Time Range</p>
        </div>
      </div>

      {/* ── Charts Grid ───────────────────────────────────────── */}
      <div className="grid grid-cols-1 lg:grid-cols-2 gap-6 mb-6">

        {/* ── Line Chart: Clicks Over Time ──────────────────── */}
        <div className="bg-white rounded-lg shadow p-4">
          <h3 className="font-medium mb-4">Clicks Over Time</h3>
          {/* ResponsiveContainer makes the chart fill its parent's width.
              Without it, you'd need to specify exact pixel dimensions. */}
          <ResponsiveContainer width="100%" height={250}>
            <LineChart data={stats?.clicksByDay || []}>
              {/* CartesianGrid adds the dotted background lines */}
              <CartesianGrid strokeDasharray="3 3" />
              {/* XAxis: horizontal axis. dataKey="date" means use the 'date' field */}
              <XAxis dataKey="date" tick={{ fontSize: 12 }} />
              {/* YAxis: vertical axis. Auto-scales based on data */}
              <YAxis tick={{ fontSize: 12 }} />
              {/* Tooltip: shows values when you hover over a data point */}
              <Tooltip />
              {/* Line: the actual line. dataKey="clicks" = which field to plot */}
              <Line type="monotone" dataKey="clicks" stroke="#3B82F6" strokeWidth={2} />
            </LineChart>
          </ResponsiveContainer>
        </div>

        {/* ── Pie Chart: Device Breakdown ───────────────────── */}
        <div className="bg-white rounded-lg shadow p-4">
          <h3 className="font-medium mb-4">Devices</h3>
          <ResponsiveContainer width="100%" height={250}>
            <PieChart>
              {/* Pie: the circular chart.
                  dataKey="count" = size of each slice
                  nameKey="name" = label for each slice
                  cx/cy = center position (50% = centered)
                  outerRadius = size of the pie */}
              <Pie
                data={stats?.deviceBreakdown || []}
                dataKey="count"
                nameKey="name"
                cx="50%"
                cy="50%"
                outerRadius={80}
                label={({ name, percent }) => `${name} ${(percent * 100).toFixed(0)}%`}
              >
                {/* Each slice gets a color from our COLORS array.
                    The modulo (%) wraps around if there are more slices than colors. */}
                {(stats?.deviceBreakdown || []).map((_, index) => (
                  <Cell key={index} fill={COLORS[index % COLORS.length]} />
                ))}
              </Pie>
              <Tooltip />
            </PieChart>
          </ResponsiveContainer>
        </div>

        {/* ── Bar Chart: Top Countries ──────────────────────── */}
        <div className="bg-white rounded-lg shadow p-4">
          <h3 className="font-medium mb-4">Top Countries</h3>
          <ResponsiveContainer width="100%" height={250}>
            <BarChart data={stats?.topCountries || []}>
              <CartesianGrid strokeDasharray="3 3" />
              <XAxis dataKey="name" tick={{ fontSize: 12 }} />
              <YAxis tick={{ fontSize: 12 }} />
              <Tooltip />
              <Bar dataKey="count" fill="#3B82F6" />
            </BarChart>
          </ResponsiveContainer>
        </div>

        {/* ── Top Referrers List ─────────────────────────────── */}
        <div className="bg-white rounded-lg shadow p-4">
          <h3 className="font-medium mb-4">Top Referrers</h3>
          {(stats?.topReferrers || []).length === 0 ? (
            <p className="text-gray-500 text-sm">No referrer data yet</p>
          ) : (
            <div className="space-y-2">
              {(stats?.topReferrers || []).map((ref, i) => (
                <div key={i} className="flex items-center justify-between py-2 border-b last:border-0">
                  <span className="text-sm text-gray-700">{ref.name || 'Direct'}</span>
                  <span className="text-sm font-medium">{formatNumber(ref.count)}</span>
                </div>
              ))}
            </div>
          )}
        </div>
      </div>

      {/* ── Export button ──────────────────────────────────────── */}
      <div className="text-center">
        <button onClick={handleExport} className="border px-4 py-2 rounded-lg hover:bg-gray-100">
          Export CSV
        </button>
      </div>
    </div>
    );
}