<%@ Page Language="C#" AutoEventWireup="true" CodeFile="example_fire_sign.aspx.cs" Inherits="example_fire_sign" %>

<%@ Register tagprefix="cf" Namespace="FIRe" %>

<!DOCTYPE html>
<html xmlns="http://www.w3.org/1999/xhtml">
<head runat="server">
    <title>Firmar documento</title>
</head>
<body>
    <form id="form1" runat="server">
    <div>
        <br /><b>Id de Transaccion:</b><br /> <asp:Label runat="server" id="TransactionId"></asp:Label>
        <br /><b>URL de redireccion:</b><br /> <asp:Label runat="server" id="RedirectURL"></asp:Label>
    </div>
    </form>
</body>
</html>
