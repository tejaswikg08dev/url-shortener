import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { toast } from 'react-toastify';
import { useDropzone } from 'react-dropzone';
import Papa from 'papaparse';
import urlService from '../services/urlService';

export default function BulkUploadPage() {
    const [urls, setUrls] = useState([]);
    const [result, setResult] = useState(null);
    const [loading, setLoading] = useState(false);
    const navigate = useNavigate();

    const onDrop = (files) => {
        const file = files[0];
        if(!file) return;
        Papa.parse(file, {
            header: true,
            skipEmptyLines: true,
            complete: (results) => {
                const parsed = results.data.filter((row) => row.longUrl || row.url || row.long_url)
                    .map((row) => ({
                        longUrl: row.longUrl || row.url || row.long_url,
                        customAlias: row.customAlias || row.alias || null,
                    }));
                setUrls(parsed);
                toast.success(`Parsed ${parsed.length} URLs from CSV`);
            },
            error: () => toast.error('Failed to parse CSV. Please check the format.'),
        });
    };

    const { getRootProps, getInputProps, isDragActive } = useDropzone({ onDrop, accept: { 'text/csv': ['.csv'] }, maxFiles: 1 });

    const handleSubmit = async () => {
        if (urls.length === 0) return;
        setLoading(true);
        try {
            const { data } = await urlService.bulkCreate(urls);
            setResult(data);
            toast.success(`Created ${data.totalSuccessful} of ${data.totalRequested} URLs`);
        } catch (err) {
            toast.error(err.response?.data?.message || 'Bulk upload failed');
        } finally {
            setLoading(false);
        }
    };

    return (
        <div className="max-w-2xl mx-auto">
      <h1 className="text-2xl font-bold mb-6">Bulk Upload</h1>

      {!result ? (
        // ── Upload + Preview View ────────────────────────────────
        <>
          {/* Drop zone container.
              {...getRootProps()} spreads all the drag-and-drop event handlers.
              The div becomes a drop target and also clickable (opens file dialog). */}
          <div
            {...getRootProps()}
            className={`border-2 border-dashed rounded-lg p-12 text-center cursor-pointer transition-colors
              ${isDragActive
                ? 'border-blue-500 bg-blue-50'      // Highlight when dragging over
                : 'border-gray-300 hover:border-blue-400'  // Default state
              }`}
          >
            {/* Hidden file input — configured by getInputProps() */}
            <input {...getInputProps()} />
            <p className="text-gray-600">
              {isDragActive
                ? 'Drop the CSV file here...'
                : 'Drag & drop a CSV file, or click to select'}
            </p>
            <p className="text-xs text-gray-400 mt-2">
              CSV format: longUrl (required), customAlias (optional)
            </p>
          </div>

          {/* Preview of parsed URLs — shown after file is parsed */}
          {urls.length > 0 && (
            <div className="mt-6">
              <h3 className="font-medium mb-2">Preview ({urls.length} URLs)</h3>
              <div className="bg-gray-50 rounded-lg p-4 max-h-60 overflow-y-auto">
                {/* Show first 10 URLs as preview */}
                {urls.slice(0, 10).map((u, i) => (
                  <div key={i} className="text-sm text-gray-600 py-1 border-b last:border-0">
                    {u.longUrl}
                    {u.customAlias && (
                      <span className="text-blue-600"> → {u.customAlias}</span>
                    )}
                  </div>
                ))}
                {/* Show count of remaining URLs if more than 10 */}
                {urls.length > 10 && (
                  <p className="text-xs text-gray-400 mt-2">
                    ...and {urls.length - 10} more
                  </p>
                )}
              </div>

              {/* Submit button */}
              <button
                onClick={handleSubmit}
                disabled={loading}
                className="mt-4 w-full bg-blue-600 text-white py-2 rounded-lg hover:bg-blue-700 disabled:opacity-50"
              >
                {loading ? 'Creating...' : `Create ${urls.length} Short Links`}
              </button>
            </div>
          )}
        </>
      ) : (
        // ── Results View (after bulk creation) ────────────────────
        <div>
          {/* Success/Failure summary cards */}
          <div className="grid grid-cols-2 gap-4 mb-6">
            <div className="bg-green-50 rounded-lg p-4 text-center">
              <p className="text-2xl font-bold text-green-600">{result.totalSuccessful}</p>
              <p className="text-sm text-gray-600">Successful</p>
            </div>
            <div className="bg-red-50 rounded-lg p-4 text-center">
              <p className="text-2xl font-bold text-red-600">{result.totalFailed}</p>
              <p className="text-sm text-gray-600">Failed</p>
            </div>
          </div>

          {/* Show details of failed URLs */}
          {result.failed?.length > 0 && (
            <div className="mb-6">
              <h3 className="font-medium mb-2 text-red-600">Failed URLs:</h3>
              {result.failed.map((f, i) => (
                <div key={i} className="text-sm text-gray-600 py-1">
                  Row {f.index + 1}: {f.longUrl} —{' '}
                  <span className="text-red-500">{f.error}</span>
                </div>
              ))}
            </div>
          )}

          {/* Navigate to dashboard */}
          <button
            onClick={() => navigate('/dashboard')}
            className="w-full bg-blue-600 text-white py-2 rounded-lg hover:bg-blue-700"
          >
            Go to Dashboard
          </button>
        </div>
      )}
    </div>
    );
}