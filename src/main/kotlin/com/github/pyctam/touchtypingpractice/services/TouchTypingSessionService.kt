package com.github.pyctam.touchtypingpractice.services

import com.intellij.openapi.components.Service
import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.project.Project

/**
 * Project-level service for managing Touch Typing Practice session state and statistics.
 *
 * Created once per project. Intended as the home for tracking typing sessions (duration, WPM,
 * accuracy), persisting session history, and exposing analytics.
 *
 * Currently a scaffold: the session-tracking features are not yet implemented.
 */
// Intentional scaffold for future session tracking (see ABOUT.md / FIXES-v1.md); not yet
// referenced.
@Suppress("unused")
@Service(Service.Level.PROJECT)
class TouchTypingSessionService(project: Project) {

  init {
    thisLogger()
      .info("Touch Typing Practice session service initialized for project: ${project.name}")
  }

  // TODO: Add session state management, statistics tracking, and persistence.
  // Examples:
  // - Track current typing session duration
  // - Store session statistics (WPM, accuracy, etc.)
  // - Manage session history and analytics
  // - Integrate with project-specific configurations
}
