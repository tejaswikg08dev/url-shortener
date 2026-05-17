export default function Pagination({ page, totalPages, onPageChange }) {
    if(totalPages <= 1) return null;

    return (
      <div className="flex items-center justify-center gap-2 mt-6">
      <button
        onClick={() => onPageChange(page - 1)}
        disabled={page === 0}  // Can't go before page 0
        className="px-3 py-1 rounded border disabled:opacity-50 hover:bg-gray-100"
      >
        Previous
      </button>

      <span className="text-sm text-gray-600">
        {/* Display 1-based page numbers for humans.
            API uses 0-based (page 0 = first page).
            Humans expect 1-based (page 1 = first page). */}
        Page {page + 1} of {totalPages}
      </span>

      <button
        onClick={() => onPageChange(page + 1)}
        disabled={page >= totalPages - 1}  // Can't go past last page
        className="px-3 py-1 rounded border disabled:opacity-50 hover:bg-gray-100"
      >
        Next
      </button>
    </div>

    );

}