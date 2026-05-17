import { format, formatDistanceToNow } from 'date-fns';

export const formatDate = (dateStr) => {
    if(!dateStr) return '-';
    return format(new Date(dateStr), 'MMM d, yyyy');
};

export const formatDateTIme = (dateStr) => {
    if(!dateStr) return '-';
    return format(new Date(dateStr), 'MMM d, yyyy HH:mm');
};

export const formatRelative = (dateStr) => {
    if(!dateStr) return '-';
    return formatDistanceToNow(new Date(dateStr), { addSuffix: true});
};

export const formatNumber = (num) => {
    if(num >= 1000000) return (num / 1000000).toFixed(1) + 'M';
    if(num >= 1000) return (num / 1000).toFixed(1) + 'K';
    return num.toString();
};

export const truncateUrl = (url, maxLength = 50) => {
    if(!url || url.length <= maxLength) return url;
    return url.substring(0, maxLength) + '...';
};