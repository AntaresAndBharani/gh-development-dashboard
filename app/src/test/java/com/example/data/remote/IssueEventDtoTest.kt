package com.example.data.remote

import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

class IssueEventDtoTest {

    private lateinit var moshi: Moshi

    @Before
    fun setUp() {
        moshi = Moshi.Builder()
            .add(KotlinJsonAdapterFactory())
            .build()
    }

    @Test
    fun parsesLabeledEventCorrectly() {
        val json = """
            {
                "id": 12345678,
                "event": "labeled",
                "created_at": "2026-08-27T10:00:00Z",
                "label": {
                    "name": "status:in-progress",
                    "color": "0075ca"
                }
            }
        """.trimIndent()

        val adapter = moshi.adapter(IssueEventDto::class.java)
        val eventDto = adapter.fromJson(json)

        assertNotNull(eventDto)
        assertEquals("labeled", eventDto?.event)
        assertEquals("2026-08-27T10:00:00Z", eventDto?.createdAt)
        assertNotNull(eventDto?.label)
        assertEquals("status:in-progress", eventDto?.label?.name)
        assertEquals("0075ca", eventDto?.label?.color)
    }

    @Test
    fun parsesEventWithoutLabelCorrectly() {
        val json = """
            {
                "id": 87654321,
                "event": "closed",
                "created_at": "2026-08-27T12:30:00Z"
            }
        """.trimIndent()

        val adapter = moshi.adapter(IssueEventDto::class.java)
        val eventDto = adapter.fromJson(json)

        assertNotNull(eventDto)
        assertEquals("closed", eventDto?.event)
        assertEquals("2026-08-27T12:30:00Z", eventDto?.createdAt)
        assertNull(eventDto?.label)
    }

    @Test
    fun parsesEventListCorrectly() {
        val json = """
            [
                {
                    "event": "labeled",
                    "created_at": "2026-08-27T09:00:00Z",
                    "label": {
                        "name": "status:definition",
                        "color": "ededed"
                    }
                },
                {
                    "event": "unlabeled",
                    "created_at": "2026-08-27T11:00:00Z",
                    "label": {
                        "name": "status:definition",
                        "color": "ededed"
                    }
                },
                {
                    "event": "labeled",
                    "created_at": "2026-08-27T11:00:00Z",
                    "label": {
                        "name": "status:ready-for-architect",
                        "color": "fbca04"
                    }
                }
            ]
        """.trimIndent()

        val type = Types.newParameterizedType(List::class.java, IssueEventDto::class.java)
        val adapter = moshi.adapter<List<IssueEventDto>>(type)
        val events = adapter.fromJson(json)

        assertNotNull(events)
        assertEquals(3, events?.size)
        assertEquals("labeled", events?.get(0)?.event)
        assertEquals("status:definition", events?.get(0)?.label?.name)
        assertEquals("unlabeled", events?.get(1)?.event)
        assertEquals("status:definition", events?.get(1)?.label?.name)
        assertEquals("labeled", events?.get(2)?.event)
        assertEquals("status:ready-for-architect", events?.get(2)?.label?.name)
    }
}
