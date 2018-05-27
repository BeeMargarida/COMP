.class public programa1
.super java/lang/Object

.method public static det([I)V
.limit locals 6
.limit stack 2
iconst_0
istore_1


aload_0
arraylength
iconst_1
istore_2


loop0:

iload_1
iload_2
if_icmpge loop0_end

aload_0
iload_1
iaload
istore_3


iload_1
iconst_1
iadd
istore_1


aload_0
iload_1
iaload
istore_5


aload_3
aload 5
istore_6


aload_3
aload 5
istore_7


goto loop0

loop0_end:


return
.end method


.method public static main([Ljava/lang/String;)V
.limit locals 0
.limit stack 0
invokestatic programa1/det(null)V

ldc "max: "
invokestatic programa1/println(Ljava/lang/String;null)V

ldc "min: "
invokestatic programa1/println(Ljava/lang/String;null)V


return
.end method


.method static public <clinit>()V
.limit stack 0
.limit locals 0
return
.end method
