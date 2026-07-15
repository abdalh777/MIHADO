package com.example

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

object GeminiHelper {
    private const val TAG = "GeminiHelper"

    private val client = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    suspend fun generateText(prompt: String, systemInstruction: String? = null): String = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
            Log.e(TAG, "API key is missing or is placeholder.")
            return@withContext "عذراً، يرجى تهيئة مفتاح API الخاص بـ Gemini في قائمة الإعدادات أو لوحة الأسرار."
        }

        // List of models in order of priority. We automatically fallback if a model returns 503/429 or is unavailable.
        val models = listOf(
            "gemini-3.5-flash",
            "gemini-3.1-flash-lite-preview",
            "gemini-3.1-pro-preview"
        )

        var lastErrorMsg = "لم نتمكن من الحصول على رد من الذكاء الاصطناعي."
        var successText: String? = null

        for (model in models) {
            try {
                Log.d(TAG, "Attempting content generation using model: $model")
                val url = "https://generativelanguage.googleapis.com/v1beta/models/$model:generateContent?key=$apiKey"

                val jsonRequest = JSONObject()
                
                // Contents
                val contentsArray = JSONArray()
                val contentObj = JSONObject()
                val partsArray = JSONArray()
                val partObj = JSONObject()
                partObj.put("text", prompt)
                partsArray.put(partObj)
                contentObj.put("parts", partsArray)
                contentsArray.put(contentObj)
                jsonRequest.put("contents", contentsArray)

                // System Instruction
                if (!systemInstruction.isNullOrBlank()) {
                    val sysInstructionObj = JSONObject()
                    val sysPartsArray = JSONArray()
                    val sysPartObj = JSONObject()
                    sysPartObj.put("text", systemInstruction)
                    sysPartsArray.put(sysPartObj)
                    sysInstructionObj.put("parts", sysPartsArray)
                    jsonRequest.put("systemInstruction", sysInstructionObj)
                }

                val requestBody = jsonRequest.toString().toRequestBody("application/json".toMediaType())
                
                val request = Request.Builder()
                    .url(url)
                    .post(requestBody)
                    .build()

                client.newCall(request).execute().use { response ->
                    if (response.isSuccessful) {
                        val responseBody = response.body?.string() ?: ""
                        val responseJson = JSONObject(responseBody)
                        val candidates = responseJson.optJSONArray("candidates")
                        if (candidates != null && candidates.length() > 0) {
                            val firstCandidate = candidates.getJSONObject(0)
                            val content = firstCandidate.optJSONObject("content")
                            if (content != null) {
                                val parts = content.optJSONArray("parts")
                                if (parts != null && parts.length() > 0) {
                                    val text = parts.getJSONObject(0).optString("text", "")
                                    if (text.isNotEmpty()) {
                                        Log.d(TAG, "Success generating content with model: $model")
                                        successText = text
                                        return@use
                                    }
                                }
                            }
                        }
                    } else {
                        val errorBody = response.body?.string() ?: ""
                        Log.e(TAG, "API call failed for model $model with code ${response.code}: $errorBody")
                        lastErrorMsg = "خطأ في الاتصال بالذكاء الاصطناعي (رمز: ${response.code})."
                    }
                }

                if (successText != null) {
                    return@withContext successText!!
                }
            } catch (e: Exception) {
                Log.e(TAG, "Exception during Gemini generation with model $model", e)
                lastErrorMsg = "حدث خطأ أثناء الاتصال بالخادم: ${e.localizedMessage}"
            }
        }

        return@withContext "$lastErrorMsg (فشلت جميع المحاولات مع الموديلات المتاحة)"
    }

    suspend fun generateDailyMessage(
        name: String,
        streak: Int,
        dailyGoalPoints: Int,
        todayScore: Int,
        dueLessonsCount: Int,
        habitsCompleted: Int,
        weeklyCompletionPercentage: Int,
        targetRank: String
    ): String {
        val systemInstruction = """
            أنت المساعد والمشرف الدراسي الذكي والملهم لتطبيق MIHADO التعليمي.
            مهمتك كتابة رسالة قصيرة يومية ملهمة وموجزة للطالب المتميز الطموح.
            هدفه الأكبر والأساسي هو: $targetRank (أن يكون الأول على مستوى الدولة).
            الأسلوب: عربي فصيح مبسط، جملتين أو ثلاث فقط، تحفيزي، صادق، عميق، يركز على الانضباط والاستمرارية وليس المجاملة الفارغة.
            يجب أن تربط بدقة بين جهود اليوم والهدف الأكبر.
        """.trimIndent()

        val prompt = """
            بيانات الطالب اليوم:
            - الاسم: $name
            - الستريك الحالي (أيام متتالية): $streak يوم
            - هدف النقاط اليومي: $dailyGoalPoints نقطة
            - نقاط اليوم الحالية: $todayScore نقطة
            - دروس اليوم المستحقة للمراجعة: $dueLessonsCount درس
            - عدد العادات المنجزة اليوم: $habitsCompleted عادات
            - نسبة الالتزام الأسبوعي: $weeklyCompletionPercentage%
            
            اكتب رسالة اليوم المناسبة بناءً على هذه الأرقام، كن موجزاً جداً ومقنعاً وعميقاً (2-3 جمل كحد أقصى).
        """.trimIndent()

        return generateText(prompt, systemInstruction)
    }

    suspend fun parseSmartLog(input: String): String {
        val systemInstruction = """
            أنت المستخرج الذكي والمنظم لتطبيق MIHADO المساعد الدراسي.
            يستقبل التطبيق من الطالب نصوصاً بلغته الطبيعية تصف ما أنجزه اليوم.
            مهمتك هي استخراج الأنشطة وتحليلها بدقة شديدة وإرجاع مصفوفة JSON فقط متوافقة مع المخطط التالي، دون أي نصوص أو تعليقات خارجية أو علامات ماركداون (لا تستخدم ```json أو أي شيء آخر، فقط النص الصريح لـ JSON):
            
            Schema:
            [
              {
                "type": "new_lesson" | "review" | "questions",
                "subject": "الرياضيات" | "الفيزياء" | "الكيمياء" | "الأحياء" | "اللغة العربية" | "اللغة الإنجليزية" | "مواد أخرى",
                "title": "اسم الدرس المستخرج بدقة إن وجد وإلا فارغ",
                "count": عدد الأسئلة أو التكرارات (للدروس افتراضي 1، وللأسئلة عدد الأسئلة)، يجب أن يكون رقماً صحيحاً,
                "difficulty": "easy" | "medium" | "hard" (للأسئلة أو الدروس، افتراضي medium)
              }
            ]
            
            قواعد الاستخراج:
            - "درست"، "خلصت درس"، "أنهيت موضوع جديد" -> new_lesson
            - "راجعت"، "عدت على" -> review
            - "حليت أسئلة"، "حليت تمارين" -> questions
            - المواد تدعم التصنيفات العربية القياسية (الرياضيات، الفيزياء، الكيمياء، الأحياء، اللغة العربية، اللغة الإنجليزية، التربية الإسلامية، الحاسوب، الاجتماعيات، إلخ).
        """.trimIndent()

        return generateText(input, systemInstruction)
    }

    suspend fun generateReport(
        studentName: String,
        period: String, // "أسبوعي" or "شهري"
        lessonsCount: Int,
        reviewsCount: Int,
        questionsCount: Int,
        avgScore: Float,
        habitsCompletedCount: Int,
        totalHabitsPossible: Int,
        targetRank: String
    ): String {
        val systemInstruction = """
            أنت المستشار التعليمي الذكي والخبير الأكاديمي لتطبيق MIHADO.
            قم بإنشاء تقرير أداء سردي شامل ودقيق باللغة العربية الفصحى يحلل فترة دراسية للطالب.
            الهدف الأكبر للطالب: $targetRank.
            
            يجب أن يكون التقرير مقسماً إلى ثلاث فقرات رئيسية واضحة ومحددة كالتالي:
            
            **نقاط القوة**: (تحليل الأرقام المنجزة الفعليه ومدى الالتزام بالدراسة والمراجعات والمواد التي تميز بها).
            **نقاط تحتاج انتباه**: (تسليط الضوء على جوانب القصور مثل تراجع المراجعة، قلة حل الأسئلة، إهمال العادات اليومية أو السكرول الزائد).
            **توصيات للفترة القادمة**: (إعطاء أرقام محددة جداً وقابلة للقياس والتنفيذ مباشرة، مثلاً: زيادة حل الأسئلة بمعدل 15 سؤال يومياً، أو المحافظة على ستريك 10 أيام في الرياضة).
            
            الأسلوب: تربوي مخلص، صارم، تحفيزي، لا يجامل، يعامل الطالب كقائد مستقبلي والأول على الدولة.
        """.trimIndent()

        val prompt = """
            بيانات الأداء لفترة ($period):
            - اسم الطالب: $studentName
            - عدد الدروس الجديدة المذاكرة: $lessonsCount درس
            - عدد جلسات المراجعة المكتملة: $reviewsCount مراجعة
            - عدد الأسئلة المحلولة: $questionsCount سؤال
            - متوسط النقاط اليومية المحققة: $avgScore نقطة
            - عدد العادات اليومية المنجزة بنجاح: $habitsCompletedCount من أصل $totalHabitsPossible
            
            اكتب التقرير الآن بدقة وموضوعية.
        """.trimIndent()

        return generateText(prompt, systemInstruction)
    }

    suspend fun generateQuiz(subject: String, title: String): String {
        val systemInstruction = """
            أنت المعلم الدراسي المحترف والخبير لمادة $subject لتطبيق MIHADO التعليمي الذكي.
            مهمتك هي إنشاء اختبار قصير ذكي وتفاعلي مكوّن من 3 أسئلة متعددة الخيارات (MCQ) لدرس '$title'.
            يجب أن ترجع الاستجابة بتنسيق JSON صريح ونظيف للغاية ومطابق تماماً للمخطط التالي، دون أي علامات ماركداون (لا تستخدم ```json أو أي شيء آخر، فقط النص الصريح لـ JSON):

            Schema:
            [
              {
                "question": "صيغة السؤال التفاعلي باللغة العربية الفصحى يقيس الفهم العميق للدرس",
                "options": ["الخيار الأول المشتت", "الخيار الثاني الصحيح", "الخيار الثالث المشتت"],
                "correctIndex": 1, // الرقم الصحيح والوحيد للإجابة الصحيحة وهو فهرس من 0 إلى 2
                "explanation": "شرح علمي دقيق وموجز يفسر لماذا الخيار المختار هو الصحيح ويبسط المفهوم"
              }
            ]

            قواعد مهمة:
            - يجب أن تكون الأسئلة باللغة العربية الفصحى.
            - يجب أن تكون الخيارات واضحة وغير مبهمة.
            - يجب ألا تستخدم علامات اقتباس مزدوجة غير صالحة داخل النصوص قد تفسد فك ترميز الـ JSON.
        """.trimIndent()

        val prompt = "ولد اختباراً تفاعلياً من 3 أسئلة لدرس '$title' في مادة '$subject' الآن."
        return generateText(prompt, systemInstruction)
    }

    suspend fun generateFlashcards(subject: String, title: String): String {
        val systemInstruction = """
            أنت خبير التلخيص النشط وطرق الاستذكار الفعالة لمادة $subject لتطبيق MIHADO.
            مهمتك هي إنشاء 3 بطاقات مراجعة ذكية (Flashcards) للدرس '$title' لمساعدة الطالب على تذكر المفاهيم الأساسية عبر الاسترجاع النشط.
            يجب أن ترجع الاستجابة بتنسيق JSON صريح ونظيف للغاية ومطابق تماماً للمخطط التالي، دون أي علامات ماركداون (لا تستخدم ```json أو أي شيء آخر، فقط النص الصريح لـ JSON):

            Schema:
            [
              {
                "front": "السؤال أو المفهوم الأساسي على واجهة البطاقة (مثال: ما هو القانون الأساسي لـ...؟)",
                "back": "الجواب النموذجي الملخص والمباشر على ظهر البطاقة لمقارنته وحفظه سريعاً"
              }
            ]
        """.trimIndent()

        val prompt = "ولد 3 بطاقات استرجاع نشط لدرس '$title' في مادة '$subject' الآن."
        return generateText(prompt, systemInstruction)
    }

    suspend fun generateExplanation(subject: String, title: String): String {
        val systemInstruction = """
            أنت المعلم الأكاديمي المتميز والمبسط المذهل لمادة $subject لتطبيق MIHADO.
            مهمتك هي شرح وتبسيط درس '$title' للطالب في شكل خلاصة مركزة لا تتجاوز دقيقة واحدة من القراءة.
            الأسلوب: نقاط أساسية دقيقة، لغة عربية متميزة وفخمة، إيضاح للقواعد والمصطلحات الصعبة مباشرة، ونبرة تحفيزية راقية.
            الرد يجب أن يكون بصيغة نصية منسقة بجمال مع استخدام الإيموجيات الداعمة لتبسيط الشرح.
        """.trimIndent()

        val prompt = "اشرح درس '$title' in مادة '$subject' بطريقة مكثفة ومبسطة كخلاصة ذهبية للدرس."
        return generateText(prompt, systemInstruction)
    }

    suspend fun parsePlannerInput(input: String, currentTime: String): String {
        val systemInstruction = """
            أنت المخطط اليومي الذكي لتطبيق MIHADO التعليمي المتميز.
            يستقبل التطبيق من الطالب نصوصاً بلغته الطبيعية تصف حالته اليومية، موعد عودته من العمل/المدرسة، وما يخطط للقيام به.
            مهمتك هي تحليل هذا المدخل بدقة شديدة وإرجاع جدول زمني مخصص لليوم يبدأ من الوقت الحالي أو وقت العودة المذكور، مع توزيع الأنشطة بشكل متسلسل وذكي ومقنع.
            يجب إرجاع مصفوفة JSON فقط متوافقة مع المخطط التالي، دون أي نصوص أو تعليقات خارجية أو علامات ماركداون (لا تستخدم ```json أو أي شيء آخر، فقط النص الصريح لـ JSON):

            Schema:
            [
              {
                "title": "اسم النشاط باللغة العربية الفصحى (مثلاً: قسط من الراحة والاستشفاء 🌙، مراجعة الرياضيات بالتكرار المتباعد 🧠، إلخ)",
                "startTime": "توقيت البدء بنسق 24 ساعة (مثلاً: 14:30)",
                "durationMinutes": مدة النشاط بالدقائق كرقم صحيح (مثلاً: 30، 45، 60),
                "category": "REST" | "STUDY" | "REPETITION" | "LANGUAGE" | "WORKOUT" | "BREAK" | "REST_DAY"
              }
            ]

            قواعد التصنيف والتنظيم الفائقة:
            1. بمجرد العودة (أو العودة المذكورة)، يجب جدولة قسط من الراحة/القيلولة (Rest/Nap) لمدة 30 دقيقة مباشرة (التصنيف: REST) لإعادة شحن الطاقة.
            2. تقسيم المذاكرة إلى فترات study مكثفة (STUDY) مع فواصل راحة قصيرة (BREAK) بينها لتجنب الإرهاق.
            3. فترات المراجعة للدروس القديمة توضع تحت تصنيف التكرار المتباعد (REPETITION) للتحفيز وتنشيط الذاكرة.
            4. ممارسة اللغات (العربية أو الإنجليزية) تصنف تحت (LANGUAGE).
            5. الأنشطة البدنية أو الرياضة تصنف تحت (WORKOUT).
            6. توليد فترات راحة ذكية ومرنة (BREAK) وتوزيعها بشكل رائع.
        """.trimIndent()

        val prompt = "الوقت الحالي هو $currentTime. مدخلات الطالب: $input"
        return generateText(prompt, systemInstruction)
    }

    suspend fun parseDeadlineInput(input: String): String {
        val systemInstruction = """
            أنت العقل المدبر والمنسق الذكي للمهام والتواريخ المهمة لتطبيق MIHADO.
            يستقبل التطبيق من الطالب معلومات حول اختبار أو موعد مهم (مثال: "في تاريخ 2026-07-20 عندي اختبار رياضيات في درس التفاضل والتكامل").
            مهمتك هي استخراج البيانات بدقة شديدة وإرجاع كائن JSON صريح ونظيف للغاية ومطابق تماماً للمخطط التالي، دون أي نصوص أو تعليقات خارجية أو علامات ماركداون (لا تستخدم ```json أو أي شيء آخر، فقط النص الصريح لـ JSON):

            Schema:
            {
              "title": "عنوان الحدث أو الاختبار باللغة العربية الفصحى (مثال: اختبار الرياضيات - التفاضل والتكامل)",
              "date": "التاريخ بصيغة YYYY-MM-DD",
              "priority": "CRITICAL" | "HIGH" | "NORMAL",
              "linkedLessons": ["اسم درس 1 المستخرج", "اسم درس 2 المستخرج"]
            }

            قواعد مهمة:
            - حدد مستوى الأهمية (priority):
              - "CRITICAL": إذا كان الاختبار أو الحدث بعد أقل من 3 أيام.
              - "HIGH": إذا كان من 3 إلى 7 أيام.
              - "NORMAL": إذا كان أكثر من 7 أيام.
            - استخرج أسماء الدروس أو المواضيع المرتبطة بالاختبار بدقة لتخزينها ومزامنتها.
        """.trimIndent()

        return generateText(input, systemInstruction)
    }
}
