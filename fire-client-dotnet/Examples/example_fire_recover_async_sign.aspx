<%@ Page Language="C#" AutoEventWireup="true" CodeFile="example_fire_recover_async_sign.aspx.cs" Inherits="example_fire_recover_async_sign" %>

<%@ Register tagprefix="cf" Namespace="FIRe" %>

<!DOCTYPE html>
<html xmlns="http://www.w3.org/1999/xhtml">
<head runat="server">
    <title>Recuperar documento firmado</title>
</head>
<body>
    <form id="form1" runat="server">
    <div>
        <br /><b>Estado:</b> <asp:Label runat="server" id="State"></asp:Label>
        <br /><b>Formato de actualizacion:</b> <asp:Label runat="server" id="Format"></asp:Label>
        <br /><b>Resultado:</b><br /> <asp:Label runat="server" id="SignatureB64"></asp:Label>
    </div>
    </form>
</body>
</html>
