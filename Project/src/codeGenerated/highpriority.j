.class public aval1
.super java/lang/Object

.method public static main([Ljava/lang/String;)V
.limit locals 4
.limit stack 2
loop0:

iload_-1
iconst_1
if_icmpge loop0_end

iaload
iadd
istore_1


iload_1
iconst_2
ishl
istore_2


goto loop0

loop0_end:


return
.end method


.method static public <clinit>()V
.limit stack 0
.limit locals 0

return
.end method
