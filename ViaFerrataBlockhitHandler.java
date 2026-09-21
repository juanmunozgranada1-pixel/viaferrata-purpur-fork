package com.viaferrata.fork.mechanics;

import io.netty.buffer.ByteBuf;

public class ViaFerrataBlockhitHandler {

    // Identificador de la espada en la mano principal
    private static final int SWORD_ITEM_CLASS = 267; // ID legacy o comprobación por Tag/Material

    /**
     * Procesa la solicitud de clic derecho en 1.7.10
     */
    public void handleLegacyRightClick(int itemInHand, int direction, ByteBuf outputBuffer, int entityId) {
        // En 1.7.10, clic derecho al aire para bloquear envía direction = 255
        if (direction == 255 && isSword(itemInHand)) {
            // 1. Inyectamos la animación de la espada bloqueando
            sendBlockhitAnimation(outputBuffer, entityId, true);

            // 2. Traducimos el paquete hacia Purpur como 'UseItem' en la Offhand (1.9+ protocol)
            sendModernOffhandUse(outputBuffer);
        }
    }

    /**
     * Cancela el bloqueo cuando el jugador suelta el clic derecho
     */
    public void handleLegacyRelease(ByteBuf outputBuffer, int entityId) {
        sendBlockhitAnimation(outputBuffer, entityId, false);
    }

    private boolean isSword(int itemId) {
        // Lógica para detectar espadas de madera, piedra, hierro, oro o diamante
        return itemId >= 267 && itemId <= 276;
    }

    private void sendBlockhitAnimation(ByteBuf buffer, int entityId, boolean blocking) {
        // Escribe en el buffer el paquete rápido de metadatos de entidad (Entity Metadata)
        // activando/desactivando el estado de 'Hand Active' de la 1.9+ mapeado visualmente a 1.7
        buffer.writeByte(0x1C); // Entity Metadata Packet ID (1.7.10)
        writeVarInt(buffer, entityId);
        
        // Metadata Index 0: Status byte (Bit 4 = Potion effect / Blocking stance)
        buffer.writeByte(0); // Index 0
        buffer.writeByte(0); // Type: Byte
        buffer.writeByte(blocking ? 0x10 : 0x00); 
        buffer.writeByte(0x7F); // End of metadata marker
    }

    private void sendModernOffhandUse(ByteBuf buffer) {
        // Estructura del paquete Serverbound 'Use Item' para la versión moderna de Purpur
        writeVarInt(buffer, 0x2F); // Modern Packet ID for UseItem
        writeVarInt(buffer, 1);    // Hand: 1 = Offhand (Escudo / Guard)
    }

    private void writeVarInt(ByteBuf buffer, int value) {
        while ((value & -128) != 0) {
            buffer.writeByte(value & 127 | 128);
            value >>>= 7;
        }
        buffer.writeByte(value);
    }
}