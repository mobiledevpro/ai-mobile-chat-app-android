---
name: web_search
description: Executes a live internet search to fetch real-time data, breaking news, current events, weather, or facts beyond your offline knowledge cutoff.
parameters: {
  "type": "OBJECT",
  "properties": {
    "query": {
      "type": "STRING",
      "description": "The search engine query string. Keep it concise and focused on core keywords."
    }
  },
  "required": ["query"]
}
---

# Capability Definition

You are running locally on a mobile device but have a pass-through pipeline to the internet. Use
this tool whenever the user asks about current events, temporal questions requiring the current
year, live statistics, or information you are uncertain about.

# Execution Constraints

- Do not attempt to guess or hallucinate real-time facts (e.g., stock prices, current weather,
  recent news).
- Optimize the `query` argument by stripping out conversational filler (e.g., instead of "can you
  look up what the weather is like in Chicago right now", output
  `query: "United Kingdom weather right now"`).

# Post-Execution Rule

Synthesize the search results cleanly. Always cite the key snippets returned from the engine
directly in your final response to the user.