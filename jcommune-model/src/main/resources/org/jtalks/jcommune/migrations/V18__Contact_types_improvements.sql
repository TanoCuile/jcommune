
# skype
UPDATE CONTACT_TYPE SET DISPLAY_PATTERN='<a href="skype:%s?chat">%s</a>' WHERE TYPE_ID = 2;

#phone number
UPDATE CONTACT_TYPE SET MASK='+380672345678', VALIDATION_PATTERN='^\\+[0-9]{5,20}$' WHERE TYPE_ID = 4;