CREATE TABLE CMVFS (
	CMFNAM varchar (255) NOT NULL,
	CMDTYP int  NOT NULL,
	CMMODD int NOT NULL,
	CMWHOM varchar (50),
	CMDATA CLOB,
	PRIMARY KEY (CMFNAM)
);

CREATE TABLE CMCHAB (
	CMUSERID varchar (50)  NOT NULL,
	CMABID varchar (50)  NOT NULL,
	CMABPF int ,
	CMABTX CLOB ,
	PRIMARY KEY (CMUSERID,CMABID)
);

CREATE TABLE CMSTAT (
	CMSTRT int NOT NULL,
	CMENDT int NOT NULL,
	CMDATA CLOB,
	PRIMARY KEY (CMSTRT)
);

CREATE TABLE CMPOLL (
	CMNAME varchar (100)  NOT NULL,
	CMBYNM varchar (100) ,
	CMSUBJ varchar (255) ,
	CMDESC CLOB ,
	CMOPTN CLOB ,
	CMFLAG int ,
	CMQUAL varchar (255) ,
	CMRESL CLOB ,
	CMEXPI int ,
	PRIMARY KEY (CMNAME)
);

CREATE TABLE CMCHAR (
    CMCHID varchar (50) NOT NULL,
	CMUSERID varchar (50)  NOT NULL ,
	CMPASS varchar (50) ,
	CMCLAS varchar (250) ,
	CMSTRE int ,
	CMRACE varchar (50) ,
	CMDEXT int ,
	CMCONS int ,
	CMGEND varchar (50) ,
	CMWISD int ,
	CMINTE int ,
	CMCHAR int ,
	CMHITP int ,
	CMLEVL varchar (50) ,
	CMMANA int ,
	CMMOVE int  ,
	CMDESC CLOB ,
	CMALIG int ,
	CMEXPE int ,
	CMEXLV int ,
	CMWORS varchar (50) ,
	CMPRAC int ,
	CMTRAI int ,
	CMAGEH int ,
	CMGOLD int ,
	CMWIMP int ,
	CMQUES int ,
	CMROID varchar (100) ,
	CMDATE varchar (50) ,
	CMCHAN int ,
	CMATTA int ,
	CMAMOR int ,
	CMDAMG int ,
	CMBTMP int ,
	CMLEIG varchar (50) ,
	CMHEIT int ,
	CMWEIT int ,
	CMPRPT varchar (250) ,
	CMCOLR varchar (255) ,
	CMLSIP varchar (100) ,
	CMEMAL varchar (255) ,
	CMPFIL CLOB ,
	CMSAVE varchar (150) ,
	CMMXML CLOB ,
	PRIMARY KEY (CMUSERID)
);

CREATE TABLE CMCHFO (
	CMUSERID varchar (50)  NOT NULL ,
	CMFONM int   NOT NULL,
	CMFOID varchar (50),
	CMFOTX CLOB,
	CMFOLV int,
	CMFOAB int ,
	PRIMARY KEY (CMUSERID,CMFONM)
);

CREATE TABLE CMCHCL (
	CMUSERID varchar (50) NOT NULL,
	CMCLAN varchar (100) NOT NULL,
	CMCLRO int,
	CMCLSTS varchar (100) ,
	PRIMARY KEY(CMUSERID, CMCLAN)
);

CREATE TABLE CMCHIT (
	CMUSERID varchar (50)  NOT NULL,
	CMITNM varchar (100)   NOT NULL,
	CMITID varchar (50),
	CMITTX CLOB,
	CMITLO varchar (100),
	CMITWO int,
	CMITUR int,
	CMITLV int,
	CMITAB int,
	CMHEIT int,
	PRIMARY KEY (CMUSERID,CMITNM)
);

CREATE TABLE CMROCH (
	CMROID varchar (50) NOT NULL,
	CMCHNM varchar (100)   NOT NULL,
	CMCHID varchar (50),
	CMCHTX CLOB,
	CMCHLV int,
	CMCHAB int,
	CMCHRE int,
	CMCHRI varchar (100) ,
	PRIMARY KEY (CMROID,CMCHNM)
);

CREATE TABLE CMROEX (
	CMROID varchar (50)   NOT NULL,
	CMDIRE int   NOT NULL,
	CMEXID varchar (50),
	CMEXTX CLOB,
	CMNRID varchar (50) ,
	PRIMARY KEY (CMROID,CMDIRE)
);

CREATE TABLE CMROIT (
	CMROID varchar (50) NOT NULL,
	CMITNM varchar (100)   NOT NULL,
	CMITID varchar (50),
	CMITLO varchar (100),
	CMITTX CLOB,
	CMITRE int,
	CMITUR int,
	CMITLV int,
	CMITAB int,
	CMHEIT int,
	PRIMARY KEY (CMROID,CMITNM)
);

CREATE TABLE CMROOM (
	CMROID varchar (50)  NOT NULL,
	CMLOID varchar (50),
	CMAREA varchar (50),
	CMDESC1 varchar (255),
	CMDESC2 CLOB,
	CMROTX CLOB , 
	PRIMARY KEY (CMROID)
);

CREATE TABLE CMQUESTS (
	CMQUESID varchar (250)  NOT NULL,
	CMQUTYPE varchar (50),
	CMQFLAGS int,
	CMQSCRPT CLOB,
	CMQWINNS CLOB ,
	PRIMARY KEY (CMQUESID)
);

CREATE TABLE CMAREA (
	CMAREA varchar (50)   NOT NULL,
	CMTYPE varchar (50) ,
	CMCLIM int,
	CMSUBS varchar (100),
	CMDESC CLOB,
	CMROTX CLOB,
	CMTECH int ,
	PRIMARY KEY (CMAREA)
);

CREATE TABLE CMJRNL (
	CMJKEY varchar (160)   NOT NULL,
	CMJRNL varchar (100),
	CMFROM varchar (50),
	CMDATE varchar (50),
	CMTONM varchar (50),
	CMSUBJ varchar (100),
	CMPART varchar (75) ,
	CMATTR int,
	CMDATA varchar (255) ,
	CMUPTM integer(8),
	CMIMGP varchar (50),
	CMVIEW int,
	CMREPL int,
	CMMSGT CLOB ,
	PRIMARY KEY (CMJKEY)
);

CREATE INDEX CMJRNLNAME on CMJRNL (CMJRNL);
CREATE INDEX CMJRNLCMPART on CMJRNL (CMPART);
CREATE INDEX CMJRNLCMTONM on CMJRNL (CMTONM);
CREATE INDEX CMJRNLCMUPTM on CMJRNL (CMUPTM);

CREATE TABLE CMCLAN (
	CMCLID varchar (100)   NOT NULL,
	CMTYPE int ,
	CMDESC CLOB,
	CMACPT varchar (255),
	CMPOLI CLOB,
	CMRCLL varchar (50),
	CMDNAT varchar (50),
	CMSTAT int,
	CMMORG varchar (50),
	CMTROP int ,
	PRIMARY KEY (CMCLID)
);

CREATE TABLE CMPDAT (
	CMPLID varchar (100)   NOT NULL,
	CMSECT varchar (100)    NOT NULL,
	CMPKEY varchar (255)    NOT NULL,
	CMPDAT CLOB , 
	PRIMARY KEY (CMPLID,CMSECT,CMPKEY)
);

CREATE TABLE CMGRAC (
	CMRCID varchar (250)   NOT NULL,
	CMRDAT CLOB ,
	CMRCDT int ,
	PRIMARY KEY (CMRCID)
);

CREATE TABLE CMCCAC (
	CMCCID varchar (50)   NOT NULL,
	CMCDAT CLOB,  
	PRIMARY KEY (CMCCID)
);

CREATE TABLE CMGAAC (
	CMGAID varchar (50)   NOT NULL,
	CMGAAT CLOB , 
	CMGACL varchar (50) ,  
	PRIMARY KEY (CMGAID)
);

CREATE TABLE CMACCT (
	CMANAM varchar (50) NOT NULL,
	CMPASS varchar (50) NOT NULL,
	CMCHRS CLOB ,
	CMAXML CLOB ,
	PRIMARY KEY (CMANAM)
);

CREATE TABLE CMBKLG (
	CMNAME varchar (50) NOT NULL,
	CMINDX int NOT NULL,
	CMSNAM int NOT NULL,
	CMDATE integer(8),
	CMDATA CLOB,
	PRIMARY KEY (CMNAME,CMINDX)
);

CREATE TABLE CMCLIT (
	CMCLID varchar (100)  NOT NULL,
	CMITNM varchar (100)   NOT NULL,
	CMITID varchar (50),
	CMITTX CLOB,
	CMITLO varchar (100),
	CMITWO int,
	CMITUR int,
	CMITLV int,
	CMITAB int,
	CMHEIT int,
	PRIMARY KEY (CMCLID,CMITNM)
);

