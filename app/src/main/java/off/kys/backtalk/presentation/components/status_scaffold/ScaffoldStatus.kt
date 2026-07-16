package off.kys.backtalk.presentation.components.status_scaffold

/**
 * Represents the visual and semantic state of a status-driven Scaffold component.
 * * This status determines the background color, text color, and accompanying icon
 * displayed within the top status banner of the layout.
 */
enum class ScaffoldStatus {
    /**
     * No status banner is displayed. The main content occupies the full layout area.
     */
    None,

    /**
     * Displays a non-critical informational banner, typically using the theme's
     * primary container and on-primary container color palette.
     */
    Info,

    /**
     * Displays a warning banner requiring user attention, typically using the theme's
     * error container or a dedicated warning color palette.
     */
    Warning,

    /**
     * Displays a critical error banner indicating a failure state, typically using
     * the theme's error and on-error color palette.
     */
    Error,

    /**
     * Displays a loading or ongoing activity banner, typically using the theme's
     * primary container and on-primary container color palette, or a dedicated
     * loading color.
     */
    Loading
}