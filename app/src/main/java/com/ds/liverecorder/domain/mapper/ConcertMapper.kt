package com.ds.liverecorder.domain.mapper

import com.ds.liverecorder.data.entity.ConcertEntity
import com.ds.liverecorder.domain.model.Concert

/**
 * Mapper class to handle conversion between ConcertEntity and Concert
 */
class ConcertMapper {
    
    fun toDomainModel(entity: ConcertEntity): Concert {
        return Concert(
            id = entity.id,
            title = entity.title,
            venue = entity.venue,
            date = entity.date,
            notes = entity.notes,
            posterResId = entity.posterResId,
            posterPath = entity.posterPath ?: "",
            ticketPrice = entity.ticketPrice,
            ticketPriceCurrency = entity.ticketPriceCurrency,
            actualPaid = entity.actualPaid,
            actualPaidCurrency = entity.actualPaidCurrency,
            otherFees = entity.otherFees,
            otherFeesCurrency = entity.otherFeesCurrency,
            performers = entity.performers,
            guests = entity.guests,
            status = entity.status,
            category = entity.category,
            rating = entity.rating
        )
    }
    
    fun toEntity(concert: Concert): ConcertEntity {
        return ConcertEntity(
            id = concert.id,
            title = concert.title,
            venue = concert.venue,
            date = concert.date,
            notes = concert.notes,
            posterResId = concert.posterResId,
            posterPath = concert.posterPath,
            ticketPrice = concert.ticketPrice,
            ticketPriceCurrency = concert.ticketPriceCurrency,
            actualPaid = concert.actualPaid,
            actualPaidCurrency = concert.actualPaidCurrency,
            otherFees = concert.otherFees,
            otherFeesCurrency = concert.otherFeesCurrency,
            performers = concert.performers,
            guests = concert.guests,
            status = concert.status,
            category = concert.category,
            rating = concert.rating
        )
    }
}