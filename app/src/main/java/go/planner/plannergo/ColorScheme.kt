package go.planner.plannergo

import android.content.Context
import android.graphics.drawable.Drawable
import androidx.core.content.ContextCompat

enum class Field {
    IP_APP_BAR_BG, //in_progress_app_bar_background
    IP_APP_BAR_TEXT, //in_progress_app_bar_text
    IP_APP_BAR_HAM, // //in_progress_app_bar_hamburger // icon
    IP_APP_BAR_OPT, // //in_progress_app_bar_options //icon
    CP_APP_BAR_BG, // //completed_app_bar_background
    CP_APP_BAR_TEXT, // //completed_app_bar_text
    CP_APP_BAR_HAM, //completed_app_bar_hamburger
    CP_APP_BAR_OPT, //completed_app_bar_options
    TR_APP_BAR_BG, //trash_app_bar_background
    TR_APP_BAR_TEXT, //trash_app_bar_text
    TR_APP_BAR_HAM, //trash_app_bar_hamburger
    TR_APP_BAR_OPT, //trash_app_bar_options
    AS_APP_BAR_BG, //assignment_app_bar_bg
    AS_APP_BAR_TEXT, //assignment_app_bar_text
    AS_APP_BAR_OPT, //assignment_app_bar_options
    AS_APP_BAR_BACK, //assignment back arrow
    AS_TEXT,
    AS_CHECK_ON,
    AS_CHECK_OFF,
    ST_APP_BAR_BG, //settings_app_bar_bg
    ST_APP_BAR_TEXT, //settings_app_bar_text
    ST_APP_BAR_BACK, //settings_app_bar_back
    ST_MAIN_BG, //settings_main_bg
    ST_MAIN_TEXT, //settings_main_text
    ST_MAIN_HEADER, //settings_header_text - might not implement
    ST_ON, //settings_toggle_on - might not implement
    ST_OFF, //settings_toggle_off - might not implement
    HF_APP_BAR_BG, //help_app_bar_header_background";
    HF_APP_BAR_TEXT, //help_app_bar_header_text";
    HF_APP_BAR_BACK,
    HF_BG, //help_background";
    HF_TEXT, //help_feedback_text";
    HF_BUTTON_BG, //help_feedback_button_background";
    HF_BUTTON_TEXT, //help_feedback_button_text";
    MAIN_BG, //main_background";
    MAIN_HEADER, //category_header_text";
    MAIN_CARD_BG, //main_card_background";
    MAIN_CARD_TEXT, //main_card_text";
    MAIN_BUTTON_BG, //main_button_background";
    MAIN_BUTTON_FG, //main_button_foreground";
    LS_APP_BAR_BG, // list app bar background
    LS_APP_BAR_TEXT,
    LS_APP_BAR_BACK,
    LS_APP_BAR_OPT,
    DW_HEAD_BG, //drawer_header_background";
    DW_HEAD_TEXT, //drawer_header_text";
    DW_BG,
    DW_IP_TEXT, //drawer_in_progress";
    DW_IP_IC, //drawer_in_progress_icon";
    DW_CP_TEXT, //drawer_completed";
    DW_CP_IC, //drawer_completed_icon";
    DW_TR_TEXT, //drawer_trash";
    DW_TR_IC, //drawer_trash_icon";
    DW_OT_TEXT, //drawer_other";
    DW_ST_IC, //drawer_settings_icon";
    DW_HF_IC, //drawer_help_feedback_icon";
    DW_SELECT_BG, // drawer select background
    DG_BG, //dialog background
    DG_HEAD_TEXT, // dialog text
    DG_TEXT
    // dialog text
}

class ColorScheme internal constructor(private val colors: HashMap<Field, Int>) {


//    operator fun get(type: Field): Int {
//        return colors[type]!!
//    }

    fun getColor(c: Context, type: Field): Int {
        return ContextCompat.getColor(c, colors[type]!!)
    }

    fun getDrawable(c: Context, type: Field): Drawable {
        return ContextCompat.getDrawable(c, colors[type]!!)!!
    }

    companion object {
        @Suppress("unused")
        private const val TAG = "ColorScheme"


        val SCHEME_DARK: ColorScheme
        val SCHEME_LIGHT: ColorScheme

        init {
            // Initialize light theme
            val lightColors = object : HashMap<Field, Int>() {
                init {
                    put(Field.IP_APP_BAR_BG, R.color.nav_color_1_bright)
                    put(Field.IP_APP_BAR_TEXT, R.color.textBlack)
                    put(Field.IP_APP_BAR_HAM, R.drawable.ic_dehaze_black_24dp)
                    put(Field.IP_APP_BAR_OPT, R.drawable.ic_more_vert_black_24dp)
                    put(Field.CP_APP_BAR_BG, R.color.nav_color_2_bright)
                    put(Field.CP_APP_BAR_TEXT, R.color.textBlack)
                    put(Field.CP_APP_BAR_HAM, R.drawable.ic_dehaze_black_24dp)
                    put(Field.CP_APP_BAR_OPT, R.drawable.ic_more_vert_black_24dp)
                    put(Field.TR_APP_BAR_BG, R.color.nav_color_3_bright)
                    put(Field.TR_APP_BAR_TEXT, R.color.textBlack)
                    put(Field.TR_APP_BAR_HAM, R.drawable.ic_dehaze_black_24dp)
                    put(Field.TR_APP_BAR_OPT, R.drawable.ic_more_vert_black_24dp)
                    put(Field.AS_APP_BAR_BG, R.color.lightPrimary)
                    put(Field.AS_APP_BAR_TEXT, R.color.textBlack)
                    put(Field.AS_APP_BAR_OPT, R.drawable.ic_save_black_24dp)
                    put(Field.AS_APP_BAR_BACK, R.drawable.ic_arrow_back_black_24dp)
                    put(Field.AS_TEXT, R.color.textBlack)
                    put(Field.AS_CHECK_ON, R.color.colorAccent)
                    put(Field.AS_CHECK_OFF, R.color.textBlack)
                    put(Field.ST_APP_BAR_BG, R.color.lightPrimary)
                    put(Field.ST_APP_BAR_TEXT, R.color.textBlack)
                    put(Field.ST_APP_BAR_BACK, R.drawable.ic_arrow_back_black_24dp)
                    put(Field.ST_MAIN_BG, R.color.lightPrimary)
                    put(Field.ST_MAIN_TEXT, R.color.textBlack)
                    put(Field.ST_MAIN_HEADER, R.color.lightAccent)
                    put(Field.ST_ON, R.color.lightAccent)
                    put(Field.ST_OFF, R.color.lightPrimary)
                    put(Field.HF_APP_BAR_BG, R.color.lightPrimary)
                    put(Field.HF_APP_BAR_TEXT, R.color.textBlack)
                    put(Field.HF_APP_BAR_BACK, R.drawable.ic_arrow_back_black_24dp)
                    put(Field.HF_BG, R.color.lightPrimary)
                    put(Field.HF_TEXT, R.color.textBlack)
                    put(Field.HF_BUTTON_BG, R.color.lightAccent)
                    put(Field.HF_BUTTON_TEXT, R.color.textBlack)
                    put(Field.MAIN_BG, R.color.lightPrimary)
                    put(Field.MAIN_HEADER, R.color.textBlack)
                    put(Field.MAIN_CARD_BG, R.color.textWhite)
                    put(Field.MAIN_CARD_TEXT, R.color.textBlack)
                    put(Field.MAIN_BUTTON_BG, R.color.lightAccent)
                    put(Field.MAIN_BUTTON_FG, R.color.textBlack)
                    put(Field.LS_APP_BAR_BG, R.color.lightPrimary)
                    put(Field.LS_APP_BAR_TEXT, R.color.textBlack)
                    put(Field.LS_APP_BAR_BACK, R.drawable.ic_arrow_back_black_24dp)
                    put(Field.LS_APP_BAR_OPT, R.drawable.ic_more_vert_black_24dp)
                    put(Field.DW_HEAD_BG, R.color.colorPrimary)
                    put(Field.DW_HEAD_TEXT, R.color.textWhite)
                    put(Field.DW_BG, R.color.lightPrimary)
                    put(Field.DW_IP_TEXT, R.color.nav_color_1)
                    put(Field.DW_IP_IC, R.drawable.ic_assignment_blue_24dp) // not implemented
                    put(Field.DW_CP_TEXT, R.color.nav_color_2)
                    put(Field.DW_CP_IC, R.drawable.ic_assignment_turned_in_purple_24dp) // not implemented
                    put(Field.DW_TR_TEXT, R.color.nav_color_3)
                    put(Field.DW_TR_IC, R.drawable.ic_delete_maroon_24dp) // not implemented
                    put(Field.DW_OT_TEXT, R.color.textGrey)
                    put(Field.DW_ST_IC, R.drawable.ic_settings_grey_24dp) // not implemented
                    put(Field.DW_HF_IC, R.drawable.ic_feedback_grey_24dp) // not implemented
                    put(Field.DW_SELECT_BG, R.color.textWhite)
                    put(Field.DG_BG, R.color.lightPrimary)
                    put(Field.DG_HEAD_TEXT, R.color.textBlack)
                    put(Field.DG_TEXT, R.color.textGrey)
                }
            }
            SCHEME_LIGHT = ColorScheme(lightColors)

            // Initialize dark theme
            val darkColors = object : HashMap<Field, Int>() {
                init {
                    put(Field.IP_APP_BAR_BG, R.color.darkPrimaryDark)
                    put(Field.IP_APP_BAR_TEXT, R.color.nav_color_1_bright)
                    put(Field.IP_APP_BAR_HAM, R.drawable.ic_dehaze_blue_24dp)
                    put(Field.IP_APP_BAR_OPT, R.drawable.ic_more_vert_blue_24dp)
                    put(Field.CP_APP_BAR_BG, R.color.darkPrimaryDark)
                    put(Field.CP_APP_BAR_TEXT, R.color.nav_color_2_bright)
                    put(Field.CP_APP_BAR_HAM, R.drawable.ic_dehaze_purple_24dp)
                    put(Field.CP_APP_BAR_OPT, R.drawable.ic_more_vert_purple_24dp)
                    put(Field.TR_APP_BAR_BG, R.color.darkPrimaryDark)
                    put(Field.TR_APP_BAR_TEXT, R.color.nav_color_3_bright)
                    put(Field.TR_APP_BAR_HAM, R.drawable.ic_dehaze_red_24dp)
                    put(Field.TR_APP_BAR_OPT, R.drawable.ic_more_vert_red_24dp)
                    put(Field.AS_APP_BAR_BG, R.color.darkPrimaryDark)
                    put(Field.AS_APP_BAR_TEXT, R.color.textWhite)
                    put(Field.AS_APP_BAR_OPT, R.drawable.ic_save_white_24dp)
                    put(Field.AS_APP_BAR_BACK, R.drawable.ic_arrow_back_white_24dp)
                    put(Field.AS_TEXT, R.color.textWhite)
                    put(Field.AS_CHECK_ON, R.color.colorAccent)
                    put(Field.AS_CHECK_OFF, R.color.textWhite)
                    put(Field.ST_APP_BAR_BG, R.color.darkPrimaryDark)
                    put(Field.ST_APP_BAR_TEXT, R.color.textWhite)
                    put(Field.ST_APP_BAR_BACK, R.drawable.ic_arrow_back_white_24dp)
                    put(Field.ST_MAIN_BG, R.color.darkPrimaryDark)
                    put(Field.ST_MAIN_TEXT, R.color.textWhite)
                    put(Field.ST_MAIN_HEADER, R.color.lightAccent)
                    put(Field.ST_ON, R.color.lightAccent)
                    put(Field.ST_OFF, R.color.darkPrimary)
                    put(Field.HF_APP_BAR_BG, R.color.darkPrimaryDark)
                    put(Field.HF_APP_BAR_TEXT, R.color.textWhite)
                    put(Field.HF_APP_BAR_BACK, R.drawable.ic_arrow_back_white_24dp)
                    put(Field.HF_BG, R.color.darkPrimaryDark)
                    put(Field.HF_TEXT, R.color.textWhite)
                    put(Field.HF_BUTTON_BG, R.color.lightAccent)
                    put(Field.HF_BUTTON_TEXT, R.color.textBlack)
                    put(Field.MAIN_BG, R.color.darkPrimaryDark)
                    put(Field.MAIN_HEADER, R.color.textWhite)
                    put(Field.MAIN_CARD_BG, R.color.darkPrimary)
                    put(Field.MAIN_CARD_TEXT, R.color.textWhite)
                    put(Field.MAIN_BUTTON_BG, R.color.lightAccent)
                    put(Field.MAIN_BUTTON_FG, R.color.textBlack)
                    put(Field.LS_APP_BAR_BG, R.color.darkPrimaryDark)
                    put(Field.LS_APP_BAR_TEXT, R.color.textWhite)
                    put(Field.LS_APP_BAR_BACK, R.drawable.ic_arrow_back_white_24dp)
                    put(Field.LS_APP_BAR_OPT, R.drawable.ic_more_vert_white_24dp)
                    put(Field.DW_HEAD_BG, R.color.darkPrimaryDark)
                    put(Field.DW_HEAD_BG, R.color.darkPrimary)
                    put(Field.DW_HEAD_TEXT, R.color.textWhite)
                    put(Field.DW_BG, R.color.darkPrimary)
                    put(Field.DW_IP_TEXT, R.color.nav_color_1)
                    put(Field.DW_IP_IC, R.drawable.ic_assignment_blue_24dp) // not implemented
                    put(Field.DW_CP_TEXT, R.color.nav_color_2)
                    put(Field.DW_CP_IC, R.drawable.ic_assignment_turned_in_purple_24dp) // not implemented
                    put(Field.DW_TR_TEXT, R.color.nav_color_3)
                    put(Field.DW_TR_IC, R.drawable.ic_delete_maroon_24dp) // not implemented
                    put(Field.DW_OT_TEXT, R.color.textWhite)
                    put(Field.DW_ST_IC, R.drawable.ic_settings_grey_24dp) // not implemented
                    put(Field.DW_HF_IC, R.drawable.ic_feedback_grey_24dp) // not implemented
                    put(Field.DW_SELECT_BG, R.color.darkPrimaryDark)
                    put(Field.DG_BG, R.color.darkPrimary)
                    put(Field.DG_HEAD_TEXT, R.color.textWhite)
                    put(Field.DG_TEXT, R.color.textGrey)
                }
            }
            SCHEME_DARK = ColorScheme(darkColors)
        }
    }
}
