const TILESET_DATA_TYPE = "text/plain/tilesetid";
const TILESET_SOURCE_LOCATION_TYPE = "text/plain/location";
const LOCATION_HAND = "HAND";
const LOCATION_TABLE = "TABLE";

function createMoveTilesEvent(event, targetLocation) {
    if (event.dataTransfer.types.includes(TILESET_DATA_TYPE)) {
        event.preventDefault();

        return {
            eventName : 'moveTiles',
            id: event.dataTransfer.getData(TILESET_DATA_TYPE),
            targetLocation: targetLocation
        }
    }
    return {}
}

export {
    TILESET_DATA_TYPE,
    TILESET_SOURCE_LOCATION_TYPE,
    LOCATION_HAND,
    LOCATION_TABLE,
    createMoveTilesEvent
}