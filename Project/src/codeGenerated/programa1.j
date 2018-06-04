.class public programa1
.super java/lang/Object

.field static data [I
.field static mx I
.field static mn I
.method public static det([I)V
.limit locals 2
.limit stack 2
iconst_0
istore_1


aload_0
arraylength
iconst_1
isub
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
istore_1


aload_0
iload_1
iaload
istore_5


iload_3
iload 5
istore_6


iload_3
iload 5
istore_7


goto loop0

loop0_end:


return
.end method


.method public static main([Ljava/lang/String;)V
.limit locals 0
.limit stack 0
getstatic programa1/data [I invokestatic programa1/det(null)V

ldc "max: "
getstatic programa1/mx I invokestatic programa1/println(Ljava/lang/String;null)V

ldc "min: "
getstatic programa1/mn I invokestatic programa1/println(Ljava/lang/String;null)V


return
.end method


.method static public <clinit>()V
.limit stack 1
.limit locals 1
bipush 100
newarray int
putstatic programa1/data [I 

return
.end method
