export function formatDateTime(
  isoString: string | null | undefined,
  options?: Intl.DateTimeFormatOptions,
): string {
  if (!isoString) return "—";
  try {
    const date = new Date(isoString);
    if (isNaN(date.getTime())) return "—";
    return new Intl.DateTimeFormat("en-IN", {
      dateStyle: "medium",
      timeStyle: "short",
      ...options,
    }).format(date);
  } catch {
    return "—";
  }
}

// Formats a UTC ISO-8601 timestamp to just the date portion in local timezone.
export function formatDate(isoString: string | null | undefined): string {
  return formatDateTime(isoString, {
    dateStyle: "medium",
    timeStyle: undefined,
  });
}

// Formats a UTC ISO-8601 timestamp to just the time portion in local timezone.
export function formatTime(isoString: string | null | undefined): string {
  return formatDateTime(isoString, {
    dateStyle: undefined,
    timeStyle: "short",
  });
}
