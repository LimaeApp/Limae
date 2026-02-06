package com.sakethh.limae.ui

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

object Icons {

    val ListAltAdd: ImageVector
        get() {
            if (_ListAltAdd != null) {
                return _ListAltAdd!!
            }
            _ListAltAdd = ImageVector.Builder(
                name = "ListAltAdd",
                defaultWidth = 24.dp,
                defaultHeight = 24.dp,
                viewportWidth = 960f,
                viewportHeight = 960f
            ).apply {
                path(fill = SolidColor(Color(0xFFE3E3E3))) {
                    moveTo(680f, 920f)
                    verticalLineToRelative(-120f)
                    lineTo(560f, 800f)
                    verticalLineToRelative(-80f)
                    horizontalLineToRelative(120f)
                    verticalLineToRelative(-120f)
                    horizontalLineToRelative(80f)
                    verticalLineToRelative(120f)
                    horizontalLineToRelative(120f)
                    verticalLineToRelative(80f)
                    lineTo(760f, 800f)
                    verticalLineToRelative(120f)
                    horizontalLineToRelative(-80f)
                    close()
                    moveTo(200f, 760f)
                    verticalLineToRelative(-560f)
                    verticalLineToRelative(560f)
                    close()
                    moveTo(200f, 840f)
                    quadToRelative(-33f, 0f, -56.5f, -23.5f)
                    reflectiveQuadTo(120f, 760f)
                    verticalLineToRelative(-560f)
                    quadToRelative(0f, -33f, 23.5f, -56.5f)
                    reflectiveQuadTo(200f, 120f)
                    horizontalLineToRelative(560f)
                    quadToRelative(33f, 0f, 56.5f, 23.5f)
                    reflectiveQuadTo(840f, 200f)
                    verticalLineToRelative(353f)
                    quadToRelative(-18f, -11f, -38f, -18f)
                    reflectiveQuadToRelative(-42f, -11f)
                    verticalLineToRelative(-324f)
                    lineTo(200f, 200f)
                    verticalLineToRelative(560f)
                    horizontalLineToRelative(280f)
                    quadToRelative(0f, 21f, 3f, 41f)
                    reflectiveQuadToRelative(10f, 39f)
                    lineTo(200f, 840f)
                    close()
                    moveTo(348.5f, 668.5f)
                    quadTo(360f, 657f, 360f, 640f)
                    reflectiveQuadToRelative(-11.5f, -28.5f)
                    quadTo(337f, 600f, 320f, 600f)
                    reflectiveQuadToRelative(-28.5f, 11.5f)
                    quadTo(280f, 623f, 280f, 640f)
                    reflectiveQuadToRelative(11.5f, 28.5f)
                    quadTo(303f, 680f, 320f, 680f)
                    reflectiveQuadToRelative(28.5f, -11.5f)
                    close()
                    moveTo(348.5f, 508.5f)
                    quadTo(360f, 497f, 360f, 480f)
                    reflectiveQuadToRelative(-11.5f, -28.5f)
                    quadTo(337f, 440f, 320f, 440f)
                    reflectiveQuadToRelative(-28.5f, 11.5f)
                    quadTo(280f, 463f, 280f, 480f)
                    reflectiveQuadToRelative(11.5f, 28.5f)
                    quadTo(303f, 520f, 320f, 520f)
                    reflectiveQuadToRelative(28.5f, -11.5f)
                    close()
                    moveTo(348.5f, 348.5f)
                    quadTo(360f, 337f, 360f, 320f)
                    reflectiveQuadToRelative(-11.5f, -28.5f)
                    quadTo(337f, 280f, 320f, 280f)
                    reflectiveQuadToRelative(-28.5f, 11.5f)
                    quadTo(280f, 303f, 280f, 320f)
                    reflectiveQuadToRelative(11.5f, 28.5f)
                    quadTo(303f, 360f, 320f, 360f)
                    reflectiveQuadToRelative(28.5f, -11.5f)
                    close()
                    moveTo(440f, 520f)
                    horizontalLineToRelative(240f)
                    verticalLineToRelative(-80f)
                    lineTo(440f, 440f)
                    verticalLineToRelative(80f)
                    close()
                    moveTo(440f, 360f)
                    horizontalLineToRelative(240f)
                    verticalLineToRelative(-80f)
                    lineTo(440f, 280f)
                    verticalLineToRelative(80f)
                    close()
                    moveTo(440f, 680f)
                    horizontalLineToRelative(54f)
                    quadToRelative(8f, -23f, 20f, -43f)
                    reflectiveQuadToRelative(28f, -37f)
                    lineTo(440f, 600f)
                    verticalLineToRelative(80f)
                    close()
                }
            }.build()

            return _ListAltAdd!!
        }

    @Suppress("ObjectPropertyName")
    private var _ListAltAdd: ImageVector? = null

    val Close: ImageVector
        get() {
            if (_Close != null) {
                return _Close!!
            }
            _Close = ImageVector.Builder(
                name = "Close",
                defaultWidth = 24.dp,
                defaultHeight = 24.dp,
                viewportWidth = 960f,
                viewportHeight = 960f
            ).apply {
                path(fill = SolidColor(Color(0xFFE3E3E3))) {
                    moveToRelative(256f, 760f)
                    lineToRelative(-56f, -56f)
                    lineToRelative(224f, -224f)
                    lineToRelative(-224f, -224f)
                    lineToRelative(56f, -56f)
                    lineToRelative(224f, 224f)
                    lineToRelative(224f, -224f)
                    lineToRelative(56f, 56f)
                    lineToRelative(-224f, 224f)
                    lineToRelative(224f, 224f)
                    lineToRelative(-56f, 56f)
                    lineToRelative(-224f, -224f)
                    lineToRelative(-224f, 224f)
                    close()
                }
            }.build()

            return _Close!!
        }

    @Suppress("ObjectPropertyName")
    private var _Close: ImageVector? = null
    val EditNote: ImageVector
        get() {
            if (_EditNote != null) {
                return _EditNote!!
            }
            _EditNote = ImageVector.Builder(
                name = "EditNote",
                defaultWidth = 24.dp,
                defaultHeight = 24.dp,
                viewportWidth = 960f,
                viewportHeight = 960f
            ).apply {
                path(fill = SolidColor(Color(0xFFE3E3E3))) {
                    moveTo(160f, 560f)
                    verticalLineToRelative(-80f)
                    horizontalLineToRelative(280f)
                    verticalLineToRelative(80f)
                    lineTo(160f, 560f)
                    close()
                    moveTo(160f, 400f)
                    verticalLineToRelative(-80f)
                    horizontalLineToRelative(440f)
                    verticalLineToRelative(80f)
                    lineTo(160f, 400f)
                    close()
                    moveTo(160f, 240f)
                    verticalLineToRelative(-80f)
                    horizontalLineToRelative(440f)
                    verticalLineToRelative(80f)
                    lineTo(160f, 240f)
                    close()
                    moveTo(520f, 800f)
                    verticalLineToRelative(-123f)
                    lineToRelative(221f, -220f)
                    quadToRelative(9f, -9f, 20f, -13f)
                    reflectiveQuadToRelative(22f, -4f)
                    quadToRelative(12f, 0f, 23f, 4.5f)
                    reflectiveQuadToRelative(20f, 13.5f)
                    lineToRelative(37f, 37f)
                    quadToRelative(8f, 9f, 12.5f, 20f)
                    reflectiveQuadToRelative(4.5f, 22f)
                    quadToRelative(0f, 11f, -4f, 22.5f)
                    reflectiveQuadTo(863f, 580f)
                    lineTo(643f, 800f)
                    lineTo(520f, 800f)
                    close()
                    moveTo(820f, 537f)
                    lineTo(783f, 500f)
                    lineTo(820f, 537f)
                    close()
                    moveTo(580f, 740f)
                    horizontalLineToRelative(38f)
                    lineToRelative(121f, -122f)
                    lineToRelative(-18f, -19f)
                    lineToRelative(-19f, -18f)
                    lineToRelative(-122f, 121f)
                    verticalLineToRelative(38f)
                    close()
                    moveTo(721f, 599f)
                    lineTo(702f, 581f)
                    lineTo(739f, 618f)
                    lineTo(721f, 599f)
                    close()
                }
            }.build()

            return _EditNote!!
        }

    @Suppress("ObjectPropertyName")
    private var _EditNote: ImageVector? = null

    val ArrowBack: ImageVector
        get() {
            if (_ArrowBack != null) {
                return _ArrowBack!!
            }
            _ArrowBack = ImageVector.Builder(
                name = "ArrowBack",
                defaultWidth = 24.dp,
                defaultHeight = 24.dp,
                viewportWidth = 960f,
                viewportHeight = 960f
            ).apply {
                path(fill = SolidColor(Color(0xFFE3E3E3))) {
                    moveToRelative(313f, 520f)
                    lineToRelative(224f, 224f)
                    lineToRelative(-57f, 56f)
                    lineToRelative(-320f, -320f)
                    lineToRelative(320f, -320f)
                    lineToRelative(57f, 56f)
                    lineToRelative(-224f, 224f)
                    horizontalLineToRelative(487f)
                    verticalLineToRelative(80f)
                    lineTo(313f, 520f)
                    close()
                }
            }.build()

            return _ArrowBack!!
        }

    @Suppress("ObjectPropertyName")
    private var _ArrowBack: ImageVector? = null
    val Settings: ImageVector
        get() {
            if (_SettingsOutlined != null) {
                return _SettingsOutlined!!
            }
            _SettingsOutlined = ImageVector.Builder(
                name = "Settings",
                defaultWidth = 24.dp,
                defaultHeight = 24.dp,
                viewportWidth = 960f,
                viewportHeight = 960f
            ).apply {
                path(fill = SolidColor(Color(0xFFE3E3E3))) {
                    moveToRelative(370f, 880f)
                    lineToRelative(-16f, -128f)
                    quadToRelative(-13f, -5f, -24.5f, -12f)
                    reflectiveQuadTo(307f, 725f)
                    lineToRelative(-119f, 50f)
                    lineTo(78f, 585f)
                    lineToRelative(103f, -78f)
                    quadToRelative(-1f, -7f, -1f, -13.5f)
                    verticalLineToRelative(-27f)
                    quadToRelative(0f, -6.5f, 1f, -13.5f)
                    lineTo(78f, 375f)
                    lineToRelative(110f, -190f)
                    lineToRelative(119f, 50f)
                    quadToRelative(11f, -8f, 23f, -15f)
                    reflectiveQuadToRelative(24f, -12f)
                    lineToRelative(16f, -128f)
                    horizontalLineToRelative(220f)
                    lineToRelative(16f, 128f)
                    quadToRelative(13f, 5f, 24.5f, 12f)
                    reflectiveQuadToRelative(22.5f, 15f)
                    lineToRelative(119f, -50f)
                    lineToRelative(110f, 190f)
                    lineToRelative(-103f, 78f)
                    quadToRelative(1f, 7f, 1f, 13.5f)
                    verticalLineToRelative(27f)
                    quadToRelative(0f, 6.5f, -2f, 13.5f)
                    lineToRelative(103f, 78f)
                    lineToRelative(-110f, 190f)
                    lineToRelative(-118f, -50f)
                    quadToRelative(-11f, 8f, -23f, 15f)
                    reflectiveQuadToRelative(-24f, 12f)
                    lineTo(590f, 880f)
                    lineTo(370f, 880f)
                    close()
                    moveTo(440f, 800f)
                    horizontalLineToRelative(79f)
                    lineToRelative(14f, -106f)
                    quadToRelative(31f, -8f, 57.5f, -23.5f)
                    reflectiveQuadTo(639f, 633f)
                    lineToRelative(99f, 41f)
                    lineToRelative(39f, -68f)
                    lineToRelative(-86f, -65f)
                    quadToRelative(5f, -14f, 7f, -29.5f)
                    reflectiveQuadToRelative(2f, -31.5f)
                    quadToRelative(0f, -16f, -2f, -31.5f)
                    reflectiveQuadToRelative(-7f, -29.5f)
                    lineToRelative(86f, -65f)
                    lineToRelative(-39f, -68f)
                    lineToRelative(-99f, 42f)
                    quadToRelative(-22f, -23f, -48.5f, -38.5f)
                    reflectiveQuadTo(533f, 266f)
                    lineToRelative(-13f, -106f)
                    horizontalLineToRelative(-79f)
                    lineToRelative(-14f, 106f)
                    quadToRelative(-31f, 8f, -57.5f, 23.5f)
                    reflectiveQuadTo(321f, 327f)
                    lineToRelative(-99f, -41f)
                    lineToRelative(-39f, 68f)
                    lineToRelative(86f, 64f)
                    quadToRelative(-5f, 15f, -7f, 30f)
                    reflectiveQuadToRelative(-2f, 32f)
                    quadToRelative(0f, 16f, 2f, 31f)
                    reflectiveQuadToRelative(7f, 30f)
                    lineToRelative(-86f, 65f)
                    lineToRelative(39f, 68f)
                    lineToRelative(99f, -42f)
                    quadToRelative(22f, 23f, 48.5f, 38.5f)
                    reflectiveQuadTo(427f, 694f)
                    lineToRelative(13f, 106f)
                    close()
                    moveTo(482f, 620f)
                    quadToRelative(58f, 0f, 99f, -41f)
                    reflectiveQuadToRelative(41f, -99f)
                    quadToRelative(0f, -58f, -41f, -99f)
                    reflectiveQuadToRelative(-99f, -41f)
                    quadToRelative(-59f, 0f, -99.5f, 41f)
                    reflectiveQuadTo(342f, 480f)
                    quadToRelative(0f, 58f, 40.5f, 99f)
                    reflectiveQuadToRelative(99.5f, 41f)
                    close()
                    moveTo(480f, 480f)
                    close()
                }
            }.build()

            return _SettingsOutlined!!
        }

    @Suppress("ObjectPropertyName")
    private var _SettingsOutlined: ImageVector? = null

    val AddNotes: ImageVector
        get() {
            if (_AddNotes != null) {
                return _AddNotes!!
            }
            _AddNotes = ImageVector.Builder(
                name = "AddNotes",
                defaultWidth = 24.dp,
                defaultHeight = 24.dp,
                viewportWidth = 960f,
                viewportHeight = 960f
            ).apply {
                path(fill = SolidColor(Color(0xFFE3E3E3))) {
                    moveTo(200f, 840f)
                    quadToRelative(-33f, 0f, -56.5f, -23.5f)
                    reflectiveQuadTo(120f, 760f)
                    verticalLineToRelative(-560f)
                    quadToRelative(0f, -33f, 23.5f, -56.5f)
                    reflectiveQuadTo(200f, 120f)
                    horizontalLineToRelative(560f)
                    quadToRelative(33f, 0f, 56.5f, 23.5f)
                    reflectiveQuadTo(840f, 200f)
                    verticalLineToRelative(268f)
                    quadToRelative(-19f, -9f, -39f, -15.5f)
                    reflectiveQuadToRelative(-41f, -9.5f)
                    verticalLineToRelative(-243f)
                    lineTo(200f, 200f)
                    verticalLineToRelative(560f)
                    horizontalLineToRelative(242f)
                    quadToRelative(3f, 22f, 9.5f, 42f)
                    reflectiveQuadToRelative(15.5f, 38f)
                    lineTo(200f, 840f)
                    close()
                    moveTo(200f, 720f)
                    verticalLineToRelative(40f)
                    verticalLineToRelative(-560f)
                    verticalLineToRelative(243f)
                    verticalLineToRelative(-3f)
                    verticalLineToRelative(280f)
                    close()
                    moveTo(280f, 680f)
                    horizontalLineToRelative(163f)
                    quadToRelative(3f, -21f, 9.5f, -41f)
                    reflectiveQuadToRelative(14.5f, -39f)
                    lineTo(280f, 600f)
                    verticalLineToRelative(80f)
                    close()
                    moveTo(280f, 520f)
                    horizontalLineToRelative(244f)
                    quadToRelative(32f, -30f, 71.5f, -50f)
                    reflectiveQuadToRelative(84.5f, -27f)
                    verticalLineToRelative(-3f)
                    lineTo(280f, 440f)
                    verticalLineToRelative(80f)
                    close()
                    moveTo(280f, 360f)
                    horizontalLineToRelative(400f)
                    verticalLineToRelative(-80f)
                    lineTo(280f, 280f)
                    verticalLineToRelative(80f)
                    close()
                    moveTo(720f, 920f)
                    quadToRelative(-83f, 0f, -141.5f, -58.5f)
                    reflectiveQuadTo(520f, 720f)
                    quadToRelative(0f, -83f, 58.5f, -141.5f)
                    reflectiveQuadTo(720f, 520f)
                    quadToRelative(83f, 0f, 141.5f, 58.5f)
                    reflectiveQuadTo(920f, 720f)
                    quadToRelative(0f, 83f, -58.5f, 141.5f)
                    reflectiveQuadTo(720f, 920f)
                    close()
                    moveTo(700f, 840f)
                    horizontalLineToRelative(40f)
                    verticalLineToRelative(-100f)
                    horizontalLineToRelative(100f)
                    verticalLineToRelative(-40f)
                    lineTo(740f, 700f)
                    verticalLineToRelative(-100f)
                    horizontalLineToRelative(-40f)
                    verticalLineToRelative(100f)
                    lineTo(600f, 700f)
                    verticalLineToRelative(40f)
                    horizontalLineToRelative(100f)
                    verticalLineToRelative(100f)
                    close()
                }
            }.build()

            return _AddNotes!!
        }

    @Suppress("ObjectPropertyName")
    private var _AddNotes: ImageVector? = null

    val Search: ImageVector
        get() {
            if (_Search != null) {
                return _Search!!
            }
            _Search = ImageVector.Builder(
                name = "Search",
                defaultWidth = 24.dp,
                defaultHeight = 24.dp,
                viewportWidth = 960f,
                viewportHeight = 960f
            ).apply {
                path(fill = SolidColor(Color(0xFFE3E3E3))) {
                    moveTo(784f, 840f)
                    lineTo(532f, 588f)
                    quadToRelative(-30f, 24f, -69f, 38f)
                    reflectiveQuadToRelative(-83f, 14f)
                    quadToRelative(-109f, 0f, -184.5f, -75.5f)
                    reflectiveQuadTo(120f, 380f)
                    quadToRelative(0f, -109f, 75.5f, -184.5f)
                    reflectiveQuadTo(380f, 120f)
                    quadToRelative(109f, 0f, 184.5f, 75.5f)
                    reflectiveQuadTo(640f, 380f)
                    quadToRelative(0f, 44f, -14f, 83f)
                    reflectiveQuadToRelative(-38f, 69f)
                    lineToRelative(252f, 252f)
                    lineToRelative(-56f, 56f)
                    close()
                    moveTo(380f, 560f)
                    quadToRelative(75f, 0f, 127.5f, -52.5f)
                    reflectiveQuadTo(560f, 380f)
                    quadToRelative(0f, -75f, -52.5f, -127.5f)
                    reflectiveQuadTo(380f, 200f)
                    quadToRelative(-75f, 0f, -127.5f, 52.5f)
                    reflectiveQuadTo(200f, 380f)
                    quadToRelative(0f, 75f, 52.5f, 127.5f)
                    reflectiveQuadTo(380f, 560f)
                    close()
                }
            }.build()

            return _Search!!
        }

    @Suppress("ObjectPropertyName")
    private var _Search: ImageVector? = null
}
