.class public programa2
.super java/lang/Object

.method public static f1()I
.limit locals 2
.limit stack 4
iconst_0
istore_0


arraylength
istore_1


iload_1
newarray int
astore_2

loop0:

iload_0
arraylength
if_icmpge loop0_end

aload_2
iload_0
iload_0
iaload
iastore
iinc 0 1
goto loop0

loop0_end:


iload_2
ireturn
.end method


.method public static f2()I
.limit locals 1
.limit stack 1
newarray int
astore_0

iconst_1
istore_0



iload_0
ireturn
.end method


.method public static main([Ljava/lang/String;)V
.limit locals 3
.limit stack 5
bipush 100
newarray int
astore_1

aload_1
iload_-1
iconst_1
iastore
aload_1
iload_-1
iconst_2
iastore
iload_1
invokestatic programa2/f1(I)I

istore_2


aload_2
iconst_0
iaload
istore_3


aload_2
bipush 99
iaload
istore 4


ldc "first: "
iload_3
invokestatic io/println(Ljava/lang/String;I)V

ldc "last: "
iload 4
invokestatic io/println(Ljava/lang/String;I)V

bipush 100
invokestatic programa2/f2(I)I

istore_2


aload_2
iconst_0
iaload
istore_3


aload_2
bipush 99
iaload
istore 4


ldc "first: "
iload_3
invokestatic io/println(Ljava/lang/String;I)V

ldc "last: "
iload 4
invokestatic io/println(Ljava/lang/String;I)V


return
.end method


.method static public <clinit>()V
.limit stack 0
.limit locals 0

return
.end method