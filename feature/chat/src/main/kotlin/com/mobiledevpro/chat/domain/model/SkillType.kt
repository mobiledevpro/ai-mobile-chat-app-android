/*
 * Copyright 2025 | Dmitri Chernysh | https://github.com/dmitriy-chernysh
 *
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package com.mobiledevpro.chat.domain.model

/**
 * 
 * Created on Jun 12, 2026.
 *
 */
enum class SkillType {
    CALENDAR_SCHEDULE,
    WEB_SEARCH
}

fun SkillType.toName(): String =
    when (this) {
        SkillType.WEB_SEARCH -> "Web Search"
        SkillType.CALENDAR_SCHEDULE -> "My Schedule"
    }

fun SkillType.toLocalPath(): String =
    when (this) {
        SkillType.WEB_SEARCH -> "skills/web-search/SKILL.md"
        SkillType.CALENDAR_SCHEDULE -> "skills/calendar-schedule/SKILL.md"
    }
