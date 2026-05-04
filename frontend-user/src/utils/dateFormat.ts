/**
 * Formats a UTC ISO-8601 timestamp string to the user's local timezone.
 * All backend timestamps are stored in UTC and transmitted as ISO-8601 strings with 'Z' suffix.
 *
 * @param isoString - ISO-8601 UTC string from the backend (e.g. "2026-05-04T02:12:06.123Z")
 * @param options - Intl.DateTimeFormat options for customizing output
 * @returns Formatted date/time string in the user's local timezone
 */
export function formatDateTime(isoString: string | null | undefined, options?: Intl.DateTimeFormatOptions): string {
  if (!isoString) return '—';
  try {
    const date = new Date(isoString);
    if (isNaN(date.getTime())) return '—';
    return new Intl.DateTimeFormat('en-IN', {
      dateStyle: 'medium',
      timeStyle: 'short',
      ...options,
    }).format(date);
  } catch {
    return '—';
  }
}

/**
 * Formats a UTC ISO-8601 timestamp to just the date portion in local timezone.
 */
export function formatDate(isoString: string | null | undefined): string {
  return formatDateTime(isoString, { dateStyle: 'medium', timeStyle: undefined });
}

/**
 * Formats a UTC ISO-8601 timestamp to just the time portion in local timezone.
 */
export function formatTime(isoString: string | null | undefined): string {
  return formatDateTime(isoString, { dateStyle: undefined, timeStyle: 'short' });
}
