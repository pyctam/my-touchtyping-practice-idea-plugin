package com.github.pyctam.touchtypingpractice.ui

import com.intellij.openapi.diagnostic.Logger
import javax.swing.JTextArea
import javax.swing.event.DocumentEvent
import javax.swing.event.DocumentListener

class TouchTypingDocumentListener(private val typingArea: JTextArea) : DocumentListener {
  private val logger: Logger = Logger.getInstance(TouchTypingDocumentListener::class.java)

  override fun insertUpdate(event: DocumentEvent?) {
    val text = typingArea.text
    logger.warn("on insertUpdate: event=$event, text=$text")
  }

  override fun removeUpdate(event: DocumentEvent?) {
    val text = typingArea.text
    logger.warn("on removeUpdate: event=$event, text=$text")
  }

  override fun changedUpdate(event: DocumentEvent?) {
    // not needed for plain text
  }
}
