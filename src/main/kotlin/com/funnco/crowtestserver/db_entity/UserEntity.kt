package com.funnco.crowtestserver.db_entity

import javax.persistence.*
import org.hibernate.annotations.Type
import java.util.*

@Entity
@Table(name = "user", schema = "application", catalog = "CrowTest")
open class UserEntity {
    @Id
    @Column(name = "user_id", insertable=false, updatable=false, nullable = false)
    @Type(type = "org.hibernate.type.PostgresUUIDType")
    open var userId: UUID? = null

    @Basic
    @Column(name = "name", nullable = false)
    open var name: String? = null

    @Basic
    @Column(name = "grade", nullable = false)
    open var grade: String? = null

    @Basic
    @Column(name = "mail", nullable = false)
    open var mail: String? = null

    @Basic
    @Column(name = "password", nullable = false)
    open var password: String? = null

    @Basic
    @Column(name = "token", nullable = true)
    open var token: String? = null

    @OneToMany(mappedBy = "refUserEntity", targetEntity=UserTestEntity::class)
    open var refUserTestEntities: List<UserTestEntity>? = null

    override fun toString(): String =
        "Entity of type: ${javaClass.name} ( " +
                "userId = $userId " +
                "name = $name " +
                "grade = $grade " +
                "mail = $mail " +
                "password = $password " +
                ")"

    // constant value returned to avoid entity inequality to itself before and after it's update/merge
    override fun hashCode(): Int = 42

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as UserEntity

        if (userId != other.userId) return false
        if (name != other.name) return false
        if (grade != other.grade) return false
        if (mail != other.mail) return false
        if (password != other.password) return false

        return true
    }

}

