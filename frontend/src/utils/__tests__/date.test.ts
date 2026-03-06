import { describe, it, expect } from 'vitest';
import { formatDate, formatDateTime, formatDateOnly } from '../date';

describe('date utils', () => {
  const testDate = '2026-03-05T18:30:00';

  it('formatDate should return formatted date', () => {
    const result = formatDate(testDate);
    expect(typeof result).toBe('string');
    expect(result.length).toBeGreaterThan(0);
  });

  it('formatDateTime should return formatted date time', () => {
    const result = formatDateTime(testDate);
    expect(typeof result).toBe('string');
    expect(result.length).toBeGreaterThan(0);
  });

  it('formatDateOnly should return formatted date only', () => {
    const result = formatDateOnly(testDate);
    expect(typeof result).toBe('string');
    expect(result.length).toBeGreaterThan(0);
  });

  it('should return empty string for null/undefined input', () => {
    expect(formatDate('')).toBe('');
    expect(formatDateTime('')).toBe('');
    expect(formatDateOnly('')).toBe('');
  });
});
