package com.example.web

import android.webkit.JavascriptInterface
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Interface for communication between WebView JavaScript and Kotlin.
 */
class FormJavaScriptBridge(
    private val coroutineScope: CoroutineScope,
    private val onQuestionsDetectedCallback: (String) -> Unit,
    private val onActiveQuestionChangedCallback: (String) -> Unit,
    private val onTextSelectedCallback: (String) -> Unit
) {

    @JavascriptInterface
    fun onQuestionsDetected(jsonString: String) {
        coroutineScope.launch(Dispatchers.Main) {
            onQuestionsDetectedCallback(jsonString)
        }
    }

    @JavascriptInterface
    fun onActiveQuestionChanged(jsonString: String) {
        coroutineScope.launch(Dispatchers.Main) {
            onActiveQuestionChangedCallback(jsonString)
        }
    }

    @JavascriptInterface
    fun onTextSelected(selectedText: String) {
        coroutineScope.launch(Dispatchers.Main) {
            onTextSelectedCallback(selectedText)
        }
    }

    companion object {
        /**
         * Comprehensive JavaScript payload injected into the WebView.
         * Scans Google Forms, Microsoft Forms, and general HTML quiz pages,
         * tracking user scroll/focus and extracting question text & options.
         */
        val INJECTION_SCRIPT = """
            (function() {
                if (window.__formAssistantInjected) return;
                window.__formAssistantInjected = true;

                function extractQuestions() {
                    const questions = [];

                    // 1. Google Forms Specific Selectors
                    const gfContainers = document.querySelectorAll('.geS5n, .Qr7Oae, div[role="listitem"]');
                    if (gfContainers.length > 0) {
                        gfContainers.forEach((container, idx) => {
                            const titleEl = container.querySelector('[role="heading"], .M7eMe, .freebirdFormviewerComponentsQuestionBaseTitle, .HoRLR, .whsOnd');
                            if (!titleEl) return;
                            const title = titleEl.innerText.trim();
                            if (!title) return;

                            const descEl = container.querySelector('.g32Q8c, .M7eMe + .g32Q8c');
                            const description = descEl ? descEl.innerText.trim() : '';

                            const options = [];
                            // Multiple choice or checkboxes
                            const optionContainers = container.querySelectorAll('.docssharedWizToggleLabeledContainer, [role="radio"], [role="checkbox"], .aDTYNe, .bz0duf, .nWQGrd');
                            optionContainers.forEach((optEl, optIdx) => {
                                const text = optEl.innerText.trim();
                                if (text) {
                                    const isSelected = optEl.getAttribute('aria-checked') === 'true' || 
                                                       optEl.classList.contains('isChecked') || 
                                                       optEl.querySelector('input:checked') !== null;
                                    options.push({
                                        index: optIdx,
                                        label: String.fromCharCode(65 + optIdx),
                                        text: text,
                                        isSelected: isSelected
                                    });
                                }
                            });

                            let type = 'MULTIPLE_CHOICE';
                            if (container.querySelector('[role="checkbox"], input[type="checkbox"]')) {
                                type = 'CHECKBOX';
                            } else if (options.length === 0 && container.querySelector('input[type="text"], textarea')) {
                                type = 'TEXT_INPUT';
                            }

                            questions.push({
                                id: 'q_' + idx,
                                index: idx,
                                title: title,
                                description: description,
                                type: type,
                                options: options,
                                isRequired: container.innerText.includes('*')
                            });
                        });
                    }

                    // 2. Generic HTML Quiz / Practice Form Selectors (Fallback & Practice Forms)
                    if (questions.length === 0) {
                        const genericItems = document.querySelectorAll('.question-card, .quiz-question, fieldset, .form-question');
                        genericItems.forEach((card, idx) => {
                            const titleEl = card.querySelector('legend, h2, h3, h4, .question-title');
                            if (!titleEl) return;
                            const title = titleEl.innerText.trim();
                            const options = [];
                            const optionLabels = card.querySelectorAll('label, .option, .choice');
                            optionLabels.forEach((labelEl, optIdx) => {
                                const input = labelEl.querySelector('input') || document.getElementById(labelEl.getAttribute('for'));
                                const text = labelEl.innerText.trim();
                                options.push({
                                    index: optIdx,
                                    label: String.fromCharCode(65 + optIdx),
                                    text: text,
                                    isSelected: input ? input.checked : false
                                });
                            });

                            let type = 'MULTIPLE_CHOICE';
                            if (card.querySelector('input[type="checkbox"]')) {
                                type = 'CHECKBOX';
                            } else if (options.length === 0 && card.querySelector('input[type="text"], textarea')) {
                                type = 'TEXT_INPUT';
                            }

                            questions.push({
                                id: 'gen_' + idx,
                                index: idx,
                                title: title,
                                description: '',
                                type: type,
                                options: options,
                                isRequired: false
                            });
                        });
                    }

                    return questions;
                }

                function notifyQuestions() {
                    const qList = extractQuestions();
                    if (window.AndroidFormBridge && qList && qList.length > 0) {
                        window.AndroidFormBridge.onQuestionsDetected(JSON.stringify(qList));
                    }
                }

                // Programmatically click an option
                window.formAssistantSelectOption = function(qIndex, optIndex) {
                    // Try Google Forms container first
                    const gfContainers = document.querySelectorAll('.geS5n, .Qr7Oae, div[role="listitem"]');
                    if (gfContainers.length > qIndex) {
                        const container = gfContainers[qIndex];
                        const optionContainers = container.querySelectorAll('.docssharedWizToggleLabeledContainer, [role="radio"], [role="checkbox"], .aDTYNe, .bz0duf, .nWQGrd');
                        if (optionContainers.length > optIndex) {
                            optionContainers[optIndex].click();
                            optionContainers[optIndex].scrollIntoView({ behavior: 'smooth', block: 'center' });
                            return true;
                        }
                    }

                    // Try generic practice form
                    const genericItems = document.querySelectorAll('.question-card, .quiz-question, fieldset, .form-question');
                    if (genericItems.length > qIndex) {
                        const card = genericItems[qIndex];
                        const optionLabels = card.querySelectorAll('label, .option, .choice');
                        if (optionLabels.length > optIndex) {
                            optionLabels[optIndex].click();
                            const input = optionLabels[optIndex].querySelector('input');
                            if (input) {
                                input.checked = true;
                                input.dispatchEvent(new Event('change', { bubbles: true }));
                            }
                            card.scrollIntoView({ behavior: 'smooth', block: 'center' });
                            return true;
                        }
                    }
                    return false;
                };

                // Scroll to specific question
                window.formAssistantScrollTo = function(qIndex) {
                    const items = document.querySelectorAll('.geS5n, .Qr7Oae, div[role="listitem"], .question-card, .quiz-question, fieldset, .form-question');
                    if (items.length > qIndex) {
                        items[qIndex].scrollIntoView({ behavior: 'smooth', block: 'center' });
                        items[qIndex].style.outline = '3px solid #6750A4';
                        setTimeout(() => { items[qIndex].style.outline = ''; }, 2000);
                        return true;
                    }
                    return false;
                };

                // Listen for text selection
                document.addEventListener('selectionchange', function() {
                    const sel = window.getSelection().toString().trim();
                    if (sel && window.AndroidFormBridge) {
                        window.AndroidFormBridge.onTextSelected(sel);
                    }
                });

                // Listen for DOM changes & load
                const observer = new MutationObserver(function() {
                    notifyQuestions();
                });
                observer.observe(document.body, { childList: true, subtree: true });

                // Run immediately and after short delay for dynamic Google Forms rendering
                notifyQuestions();
                setTimeout(notifyQuestions, 1000);
                setTimeout(notifyQuestions, 2500);
            })();
        """.trimIndent()
    }
}
