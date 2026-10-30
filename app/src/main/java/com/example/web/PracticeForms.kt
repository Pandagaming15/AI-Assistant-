package com.example.web

import com.example.data.model.PresetForm

object PracticeForms {

    val PRESET_LIST = listOf(
        PresetForm(
            title = "Science & Tech Quiz",
            category = "STEM Practice",
            description = "Interactive test covering astronomy, physics, biology, and computer science",
            url = "file:///android_asset/practice_stem.html",
            isLocalPractice = true
        ),
        PresetForm(
            title = "World History & Geography",
            category = "Social Studies",
            description = "Multiple choice questions on world capitals, milestones, and civilizations",
            url = "file:///android_asset/practice_history.html",
            isLocalPractice = true
        ),
        PresetForm(
            title = "Logic & Problem Solving",
            category = "Quantitative",
            description = "Patterns, deductive reasoning, probability, and logic puzzles",
            url = "file:///android_asset/practice_logic.html",
            isLocalPractice = true
        ),
        PresetForm(
            title = "Google Forms Template Gallery",
            category = "Online",
            description = "Open Google Forms official workspace",
            url = "https://docs.google.com/forms/u/0/",
            isLocalPractice = false
        )
    )

    fun getStemQuizHtml(): String = """
        <!DOCTYPE html>
        <html>
        <head>
            <meta charset="utf-8">
            <meta name="viewport" content="width=device-width, initial-scale=1.0">
            <title>Science & Technology Assessment</title>
            <style>
                body {
                    font-family: 'Roboto', -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Helvetica, Arial, sans-serif;
                    background-color: #f0ebf8;
                    margin: 0;
                    padding: 16px;
                    color: #202124;
                }
                .container {
                    max-width: 640px;
                    margin: 0 auto;
                }
                .header-card {
                    background: #ffffff;
                    border-radius: 8px;
                    border-top: 10px solid #673ab7;
                    padding: 24px;
                    margin-bottom: 16px;
                    box-shadow: 0 1px 3px rgba(0,0,0,0.12);
                }
                .header-card h1 {
                    margin: 0 0 8px 0;
                    font-size: 24px;
                    color: #202124;
                }
                .header-card p {
                    margin: 0;
                    color: #5f6368;
                    font-size: 14px;
                    line-height: 1.5;
                }
                .question-card {
                    background: #ffffff;
                    border-radius: 8px;
                    padding: 20px;
                    margin-bottom: 16px;
                    box-shadow: 0 1px 3px rgba(0,0,0,0.12);
                    transition: border-color 0.2s;
                }
                .question-title {
                    font-size: 16px;
                    font-weight: 500;
                    margin-bottom: 16px;
                    color: #202124;
                    line-height: 1.4;
                }
                .required-star {
                    color: #d93025;
                }
                .option-row {
                    display: flex;
                    align-items: center;
                    padding: 10px 8px;
                    border-radius: 4px;
                    cursor: pointer;
                    margin-bottom: 4px;
                }
                .option-row:hover {
                    background-color: #f8f9fa;
                }
                .option-row input[type="radio"], .option-row input[type="checkbox"] {
                    margin-right: 12px;
                    accent-color: #673ab7;
                    transform: scale(1.2);
                }
                .option-text {
                    font-size: 14px;
                    color: #3c4043;
                }
                .submit-btn {
                    background-color: #673ab7;
                    color: #ffffff;
                    border: none;
                    border-radius: 4px;
                    padding: 10px 24px;
                    font-size: 14px;
                    font-weight: 500;
                    cursor: pointer;
                    box-shadow: 0 1px 2px rgba(0,0,0,0.2);
                }
                .badge {
                    display: inline-block;
                    background: #e8eaed;
                    color: #3c4043;
                    font-size: 11px;
                    padding: 2px 8px;
                    border-radius: 12px;
                    margin-bottom: 8px;
                    font-weight: bold;
                }
            </style>
        </head>
        <body>
            <div class="container">
                <div class="header-card">
                    <span class="badge">Google Forms Simulator</span>
                    <h1>Science & Technology Assessment</h1>
                    <p>Live practice quiz to demonstrate real-time AI question detection, instant answer recommendation, step-by-step reasoning, and option auto-selection.</p>
                </div>

                <!-- Question 1 -->
                <div class="question-card" id="q0">
                    <div class="question-title">
                        1. Which organelle is famously known as the powerhouse of the cell, responsible for generating most of the cell's ATP supply? <span class="required-star">*</span>
                    </div>
                    <label class="option-row">
                        <input type="radio" name="q1" value="A">
                        <span class="option-text">Ribosome</span>
                    </label>
                    <label class="option-row">
                        <input type="radio" name="q1" value="B">
                        <span class="option-text">Mitochondria</span>
                    </label>
                    <label class="option-row">
                        <input type="radio" name="q1" value="C">
                        <span class="option-text">Endoplasmic Reticulum</span>
                    </label>
                    <label class="option-row">
                        <input type="radio" name="q1" value="D">
                        <span class="option-text">Golgi Apparatus</span>
                    </label>
                </div>

                <!-- Question 2 -->
                <div class="question-card" id="q1">
                    <div class="question-title">
                        2. In physics, what is the phenomenon where the frequency of a wave appears to change for an observer moving relative to the wave source? <span class="required-star">*</span>
                    </div>
                    <label class="option-row">
                        <input type="radio" name="q2" value="A">
                        <span class="option-text">Photoelectric Effect</span>
                    </label>
                    <label class="option-row">
                        <input type="radio" name="q2" value="B">
                        <span class="option-text">Doppler Effect</span>
                    </label>
                    <label class="option-row">
                        <input type="radio" name="q2" value="C">
                        <span class="option-text">Total Internal Reflection</span>
                    </label>
                    <label class="option-row">
                        <input type="radio" name="q2" value="D">
                        <span class="option-text">Quantum Entanglement</span>
                    </label>
                </div>

                <!-- Question 3 -->
                <div class="question-card" id="q2">
                    <div class="question-title">
                        3. Which programming language runtime famously uses a Garbage Collector and compiles down to bytecode running on the JVM? <span class="required-star">*</span>
                    </div>
                    <label class="option-row">
                        <input type="radio" name="q3" value="A">
                        <span class="option-text">C++</span>
                    </label>
                    <label class="option-row">
                        <input type="radio" name="q3" value="B">
                        <span class="option-text">Rust</span>
                    </label>
                    <label class="option-row">
                        <input type="radio" name="q3" value="C">
                        <span class="option-text">Kotlin</span>
                    </label>
                    <label class="option-row">
                        <input type="radio" name="q3" value="D">
                        <span class="option-text">Assembly x86</span>
                    </label>
                </div>

                <!-- Question 4 -->
                <div class="question-card" id="q3">
                    <div class="question-title">
                        4. What is the chemical formula of ozone, the atmospheric molecule that absorbs harmful ultraviolet rays? <span class="required-star">*</span>
                    </div>
                    <label class="option-row">
                        <input type="radio" name="q4" value="A">
                        <span class="option-text">O₂</span>
                    </label>
                    <label class="option-row">
                        <input type="radio" name="q4" value="B">
                        <span class="option-text">CO₂</span>
                    </label>
                    <label class="option-row">
                        <input type="radio" name="q4" value="C">
                        <span class="option-text">O₃</span>
                    </label>
                    <label class="option-row">
                        <input type="radio" name="q4" value="D">
                        <span class="option-text">CH₄</span>
                    </label>
                </div>

                <!-- Question 5 -->
                <div class="question-card" id="q4">
                    <div class="question-title">
                        5. What is the approximate speed of light in vacuum (c)? <span class="required-star">*</span>
                    </div>
                    <label class="option-row">
                        <input type="radio" name="q5" value="A">
                        <span class="option-text">3 × 10⁸ m/s</span>
                    </label>
                    <label class="option-row">
                        <input type="radio" name="q5" value="B">
                        <span class="option-text">3 × 10⁶ m/s</span>
                    </label>
                    <label class="option-row">
                        <input type="radio" name="q5" value="C">
                        <span class="option-text">343 m/s</span>
                    </label>
                    <label class="option-row">
                        <input type="radio" name="q5" value="D">
                        <span class="option-text">9.8 m/s²</span>
                    </label>
                </div>

                <div style="margin-top: 24px; text-align: right;">
                    <button class="submit-btn" onclick="alert('Form submitted successfully!')">Submit Assessment</button>
                </div>
            </div>
        </body>
        </html>
    """.trimIndent()

    fun getHistoryQuizHtml(): String = """
        <!DOCTYPE html>
        <html>
        <head>
            <meta charset="utf-8">
            <meta name="viewport" content="width=device-width, initial-scale=1.0">
            <title>World History & Geography</title>
            <style>
                body {
                    font-family: 'Roboto', -apple-system, sans-serif;
                    background-color: #f0ebf8;
                    margin: 0;
                    padding: 16px;
                    color: #202124;
                }
                .container { max-width: 640px; margin: 0 auto; }
                .header-card {
                    background: #ffffff;
                    border-radius: 8px;
                    border-top: 10px solid #1a73e8;
                    padding: 24px;
                    margin-bottom: 16px;
                    box-shadow: 0 1px 3px rgba(0,0,0,0.12);
                }
                .header-card h1 { margin: 0 0 8px 0; font-size: 24px; }
                .question-card {
                    background: #ffffff;
                    border-radius: 8px;
                    padding: 20px;
                    margin-bottom: 16px;
                    box-shadow: 0 1px 3px rgba(0,0,0,0.12);
                }
                .question-title { font-size: 16px; font-weight: 500; margin-bottom: 16px; }
                .option-row {
                    display: flex;
                    align-items: center;
                    padding: 10px 8px;
                    border-radius: 4px;
                    cursor: pointer;
                    margin-bottom: 4px;
                }
                .option-row:hover { background-color: #f8f9fa; }
                .option-row input { margin-right: 12px; accent-color: #1a73e8; transform: scale(1.2); }
                .option-text { font-size: 14px; }
            </style>
        </head>
        <body>
            <div class="container">
                <div class="header-card">
                    <h1>World History & Geography Quiz</h1>
                    <p>Test your knowledge of world milestones, geography, and world wonders.</p>
                </div>

                <div class="question-card">
                    <div class="question-title">1. What is the capital city of Australia?</div>
                    <label class="option-row"><input type="radio" name="h1" value="A"><span class="option-text">Sydney</span></label>
                    <label class="option-row"><input type="radio" name="h1" value="B"><span class="option-text">Melbourne</span></label>
                    <label class="option-row"><input type="radio" name="h1" value="C"><span class="option-text">Canberra</span></label>
                    <label class="option-row"><input type="radio" name="h1" value="D"><span class="option-text">Brisbane</span></label>
                </div>

                <div class="question-card">
                    <div class="question-title">2. In which year did the Apollo 11 mission land humans on the Moon for the first time?</div>
                    <label class="option-row"><input type="radio" name="h2" value="A"><span class="option-text">1965</span></label>
                    <label class="option-row"><input type="radio" name="h2" value="B"><span class="option-text">1969</span></label>
                    <label class="option-row"><input type="radio" name="h2" value="C"><span class="option-text">1972</span></label>
                    <label class="option-row"><input type="radio" name="h2" value="D"><span class="option-text">1959</span></label>
                </div>

                <div class="question-card">
                    <div class="question-title">3. Which ancient civilization built the monumental Machu Picchu in Peru?</div>
                    <label class="option-row"><input type="radio" name="h3" value="A"><span class="option-text">Maya</span></label>
                    <label class="option-row"><input type="radio" name="h3" value="B"><span class="option-text">Aztec</span></label>
                    <label class="option-row"><input type="radio" name="h3" value="C"><span class="option-text">Inca</span></label>
                    <label class="option-row"><input type="radio" name="h3" value="D"><span class="option-text">Olmec</span></label>
                </div>
            </div>
        </body>
        </html>
    """.trimIndent()

    fun getLogicQuizHtml(): String = """
        <!DOCTYPE html>
        <html>
        <head>
            <meta charset="utf-8">
            <meta name="viewport" content="width=device-width, initial-scale=1.0">
            <title>Logic & Quantitative Reasoning</title>
            <style>
                body {
                    font-family: 'Roboto', -apple-system, sans-serif;
                    background-color: #f0ebf8;
                    margin: 0;
                    padding: 16px;
                    color: #202124;
                }
                .container { max-width: 640px; margin: 0 auto; }
                .header-card {
                    background: #ffffff;
                    border-radius: 8px;
                    border-top: 10px solid #0f9d58;
                    padding: 24px;
                    margin-bottom: 16px;
                    box-shadow: 0 1px 3px rgba(0,0,0,0.12);
                }
                .header-card h1 { margin: 0 0 8px 0; font-size: 24px; }
                .question-card {
                    background: #ffffff;
                    border-radius: 8px;
                    padding: 20px;
                    margin-bottom: 16px;
                    box-shadow: 0 1px 3px rgba(0,0,0,0.12);
                }
                .question-title { font-size: 16px; font-weight: 500; margin-bottom: 16px; }
                .option-row {
                    display: flex;
                    align-items: center;
                    padding: 10px 8px;
                    border-radius: 4px;
                    cursor: pointer;
                    margin-bottom: 4px;
                }
                .option-row:hover { background-color: #f8f9fa; }
                .option-row input { margin-right: 12px; accent-color: #0f9d58; transform: scale(1.2); }
                .option-text { font-size: 14px; }
            </style>
        </head>
        <body>
            <div class="container">
                <div class="header-card">
                    <h1>Logic & Quantitative Reasoning</h1>
                    <p>Analytical puzzle solving, series predictions, and mathematical logic.</p>
                </div>

                <div class="question-card">
                    <div class="question-title">1. What is the next number in the sequence: 2, 6, 12, 20, 30, ?</div>
                    <label class="option-row"><input type="radio" name="l1" value="A"><span class="option-text">36</span></label>
                    <label class="option-row"><input type="radio" name="l1" value="B"><span class="option-text">40</span></label>
                    <label class="option-row"><input type="radio" name="l1" value="C"><span class="option-text">42</span></label>
                    <label class="option-row"><input type="radio" name="l1" value="D"><span class="option-text">48</span></label>
                </div>

                <div class="question-card">
                    <div class="question-title">2. If a fair 6-sided die is rolled twice, what is the probability of rolling a sum of 7?</div>
                    <label class="option-row"><input type="radio" name="l2" value="A"><span class="option-text">1/6</span></label>
                    <label class="option-row"><input type="radio" name="l2" value="B"><span class="option-text">1/12</span></label>
                    <label class="option-row"><input type="radio" name="l2" value="C"><span class="option-text">7/36</span></label>
                    <label class="option-row"><input type="radio" name="l2" value="D"><span class="option-text">1/4</span></label>
                </div>
            </div>
        </body>
        </html>
    """.trimIndent()
}
