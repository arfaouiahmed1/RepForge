package com.repforge.app.tile

import android.service.quicksettings.TileService

// Very cheap, very slick — Android-specific delight. Tap to start workout or toggle rest timer.
class WorkoutTileService : TileService() {
    override fun onClick() {
        super.onClick()
        // TODO: if workout active -> skip rest else start today's workout via deep link repforge://workout
        // qsTile.state = Tile.STATE_ACTIVE; qsTile.updateTile()
    }
}
