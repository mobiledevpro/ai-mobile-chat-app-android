---
name: view_calendar
description: Retrieves a list of calendar events, meetings, or agenda items for a specified time range or matching a specific search query.
parameters: {
  "type": "OBJECT",
  "properties": {
    "startDate": {
      "type": "STRING",
      "description": "The starting boundary of the query in ISO 8601 date format (YYYY-MM-DD). If omitted, default to today's date."
    },
    "endDate": {
      "type": "STRING",
      "description": "The ending boundary of the query in ISO 8601 date format (YYYY-MM-DD). If checking a single day, this should equal the startDate."
    },
    "searchQuery": {
      "type": "STRING",
      "description": "Optional keyword or title phrase to filter the calendar events (e.g., 'dentist', 'sync meeting'). Leave empty if retrieving the full agenda."
    }
  },
  "required": [ "startDate", "endDate" ]
}
---

# Capability Definition

Activate this tool whenever the user inquires about their upcoming schedule, plans, meetings, or
availability. This includes queries like "what is my day looking like?", "am I free tomorrow
morning?", or "when is my next dentist appointment?".

# Temporal & Execution Constraints

- Do not make up or assume calendar data. Always pull from the native provider.
- **Handling Relative Dates:** Translate conversational time frames into exact `YYYY-MM-DD` strings
  before invoking.
  - "Today" / "Now" -> Compute the current system date.
  - "Tomorrow" -> Compute current system date + 1 day.
  - "This weekend" -> Compute the range mapping to the upcoming Saturday and Sunday.
- If the user provides a time without a date (e.g., "what am I doing at 3 PM?"), assume they mean 3
  PM of the current calendar day unless context implies otherwise.

# Post-Execution Rule

- If the tool returns an empty list, explicitly state: "You have no events scheduled for that time."
- When events are returned, sort them chronologically and present them as a clean, scannable summary
  listing titles, start/end times, and locations (if available).