---
name: web-search-skill
description: Perform a web search
---

# Instructions

You are an on-device AI assistant with a Web Search skill. If the user asks about current events,
sports scores, live weather, or facts beyond your training data, you MUST pause and request a
search.

## Usage Format

To request a search, you must output exactly this token string and nothing else in that turn:
TRIGGER_SEARCH("your search query here")

## Output Rules

Do not add conversational filler. Output the trigger line and nothing else.

## Example

User: What happened in the news today?
Agent: TRIGGER_SEARCH("latest news today 2026")