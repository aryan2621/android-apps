/**
 * Utility functions for time formatting
 */

/**
 * Converts 24-hour time string (HH:MM) to 12-hour format (H:MM AM/PM)
 * @param time24 - Time in 24-hour format (e.g., "14:30")
 * @returns Time in 12-hour format (e.g., "2:30 PM")
 */
export function formatTime12Hour(time24: string): string {
    const [hours, minutes] = time24.split(':').map(Number);
    
    if (hours === 0) {
        return `12:${minutes.toString().padStart(2, '0')} AM`;
    } else if (hours < 12) {
        return `${hours}:${minutes.toString().padStart(2, '0')} AM`;
    } else if (hours === 12) {
        return `12:${minutes.toString().padStart(2, '0')} PM`;
    } else {
        return `${hours - 12}:${minutes.toString().padStart(2, '0')} PM`;
    }
}

/**
 * Converts 12-hour time string (H:MM AM/PM) to 24-hour format (HH:MM)
 * @param time12 - Time in 12-hour format (e.g., "2:30 PM")
 * @returns Time in 24-hour format (e.g., "14:30")
 */
export function formatTime24Hour(time12: string): string {
    const [time, period] = time12.split(' ');
    const [hours, minutes] = time.split(':').map(Number);
    
    if (period === 'AM') {
        if (hours === 12) {
            return `00:${minutes.toString().padStart(2, '0')}`;
        }
        return `${hours.toString().padStart(2, '0')}:${minutes.toString().padStart(2, '0')}`;
    } else { // PM
        if (hours === 12) {
            return `12:${minutes.toString().padStart(2, '0')}`;
        }
        return `${(hours + 12).toString().padStart(2, '0')}:${minutes.toString().padStart(2, '0')}`;
    }
}
