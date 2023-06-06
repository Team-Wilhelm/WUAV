create table Customer
(
    CustomerID          uniqueidentifier default newid() not null
        primary key,
    CustomerName        nvarchar(255)                    not null,
    CustomerEmail       nvarchar(255)                    not null
        constraint Customer_pk
            unique,
    CustomerPhoneNumber nvarchar(20),
    Deleted             bit              default 0       not null,
    LastContract        date                             not null,
    CustomerType        nvarchar(10)                     not null
)
go

create table CustomerAddress
(
    AddressID    int identity
        primary key,
    StreetName   nvarchar(255) not null,
    StreetNumber nvarchar(255) not null,
    Postcode     nvarchar(10)  not null,
    Town         nvarchar(255) not null,
    Country      nvarchar(255)
)
go

create table Customer_Address_Link
(
    CustomerID uniqueidentifier not null
        references Customer
            on delete cascade,
    AddressID  int              not null
        references CustomerAddress
)
go

create table Document
(
    DocumentID     uniqueidentifier default newid() not null
        primary key,
    JobDescription nvarchar(max)                    not null,
    Notes          nvarchar(255),
    CustomerID     uniqueidentifier                 not null
        references Customer
            on delete cascade,
    Deleted        bit              default 0       not null,
    JobTitle       nvarchar(255)                    not null,
    DateOfCreation date,
    DrawingShapes  nvarchar(4000)
)
go

create table Document_Drawing_Link
(
    DocumentId uniqueidentifier not null
        constraint Document_Drawing_Link_pk
            primary key
        constraint Document_Drawing_Link_Document_DocumentID_fk
            references Document
            on delete cascade,
    Drawing    nvarchar(200)
)
go

create table Document_Image_Link
(
    DocumentID   uniqueidentifier not null
        references Document
            on delete cascade,
    Filepath     nvarchar(max)    not null,
    FileName     nvarchar(max),
    PictureIndex int,
    Description  nvarchar(256)
)
go

create table SystemUser
(
    UserID         uniqueidentifier default newid() not null
        primary key,
    FullName       nvarchar(255)                    not null,
    Username       nvarchar(255)                    not null,
    UserRole       nvarchar(50)                     not null,
    Deleted        bit              default 0       not null,
    PhoneNumber    nvarchar(30),
    UserPassword   varbinary(max)                   not null,
    ProfilePicture nvarchar(max),
    Salt           varbinary(max)
)
go

create table User_Document_Link
(
    UserID     uniqueidentifier not null
        references SystemUser,
    DocumentID uniqueidentifier not null
        references Document
            on delete cascade
)
go


