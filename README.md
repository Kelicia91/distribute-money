#distribute-money
.

## modeling
```sql
create table distributable_money (
    id bigint not null,
    created_at timestamp,
    money integer not null,
    receiver_id integer,
    distribution_id bigint,
    primary key (id)
)

create table distribution (
    id bigint not null,
    created_at timestamp,
    divisor integer not null,
    money integer not null,
    room_id varchar(255) not null,
    sender_id integer not null,
    token varchar(3) not null,
    primary key (id)
)

create index IDX_DISTRIBUTION_ROOM_ID_TOKEN on distribution (room_id, token)

alter table distributable_money 
    add constraint FK5ny17e2lhm2li03pnr332k4c0 
    foreign key (distribution_id) 
    references distribution
```