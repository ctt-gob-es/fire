<%@ Page Language="C#" AutoEventWireup="true" CodeFile="example_fire_recover_error.aspx.cs" Inherits="example_fire_recover_error" %>

<%@ Register tagprefix="cf" Namespace="FIRe" %>

<!DOCTYPE html>
<html xmlns="http://www.w3.org/1999/xhtml">
<head runat="server">
    <title>Ejemplo de recuperacion de error</title>
</head>
<body>
    <form id="form1" runat="server">
    <div>
        <br /><b>Proveedor:</b><br /> <asp:Label runat="server" id="Provider"></asp:Label>
        <br /><b>Error:</b><br /> <asp:Label runat="server" id="Error"></asp:Label>
    </div>
    </form>
</body>
</html>
